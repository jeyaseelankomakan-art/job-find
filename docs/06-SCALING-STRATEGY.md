# Job Matching Platform - Scaling Strategy for Production

## Table of Contents

1. [Current Architecture](#current-architecture)
2. [Growth Phases](#growth-phases)
3. [Database Scaling](#database-scaling)
4. [Backend Scaling](#backend-scaling)
5. [Frontend Scaling](#frontend-scaling)
6. [Search Optimization](#search-optimization)
7. [Caching Strategy](#caching-strategy)
8. [Monitoring & Observability](#monitoring--observability)
9. [Cost Optimization](#cost-optimization)

---

## Current Architecture (MVP)

```
┌─────────────────┐
│  Single EC2/VM  │
├─────────────────┤
│ Spring Boot App │
│ React (static)  │
└─────────────────┘
        ↓
┌─────────────────┐
│   MySQL RDS     │
│   (single node) │
└─────────────────┘
```

**Capacity**: ~5,000 concurrent users
**Cost**: ~$100-200/month

---

## Growth Phases

### Phase 1: MVP (0-50K Users)

**Timeline**: Months 1-3
**Infrastructure**: Single backend instance + MySQL
**Cost**: $100-300/month

**Scaling Actions**:

- Add read replicas for analytics queries
- Implement Redis caching
- Optimize database queries
- Monitor performance

---

### Phase 2: Traction (50K-500K Users)

**Timeline**: Months 4-8
**Target**: 10,000 concurrent users
**Cost**: $500-1,500/month

**Architecture**:

```
┌─────────────────────────────────────┐
│  Elastic Load Balancer (ALB)        │
└────────────────────┬────────────────┘
        │            │            │
    ┌───▼──┐   ┌─────▼──┐  ┌────▼────┐
    │ Inst │   │  Inst  │  │  Inst   │
    │  #1  │   │   #2   │  │   #3    │
    └──────┘   └────────┘  └─────────┘
         └─────────┬──────────┘
                  ↓
         ┌──────────────────┐
         │  MySQL Master    │
         └─────────┬────────┘
                  ↓
         ┌──────────────────┐
         │ MySQL Slave #1   │
         │ MySQL Slave #2   │
         └──────────────────┘
                  ↓
         ┌──────────────────┐
         │ Redis Cluster    │
         │ (3 nodes)        │
         └──────────────────┘
                  ↓
         ┌──────────────────┐
         │  Elasticsearch   │
         │  (for search)    │
         └──────────────────┘
```

**Scaling Actions**:

- Horizontal backend scaling (3+ instances)
- Implement job queues (RabbitMQ/Kafka)
- Full-text search with Elasticsearch
- Database replication + read replicas
- Redis cluster for distributed caching
- CDN for static assets (CloudFront)

---

### Phase 3: Scale (500K-5M Users)

**Timeline**: Months 9-18
**Target**: 100,000 concurrent users
**Cost**: $2,000-5,000/month

**Architecture Enhancements**:

- Microservices decomposition
- Multi-region deployment
- Database sharding
- Advanced caching layers
- Message queue scaling
- Real-time analytics

---

## Database Scaling

### Phase 1: Single Master (MVP)

```sql
-- Start with single MySQL instance
CREATE DATABASE job_matching_db;
```

**Limitations**: ~1,000 req/sec max

---

### Phase 2: Read Replicas (50K-500K)

**Master-Slave Replication**:

```
┌─────────────────┐
│ MySQL Master    │ (Write operations)
│ (Primary)       │
└────────┬────────┘
         │ (Binary Log Replication)
      ┌──┴──┬──────┐
      ↓     ↓      ↓
   ┌────┐ ┌────┐ ┌────┐
   │RS1 │ │RS2 │ │RS3 │ (Read-only replicas)
   └────┘ └────┘ └────┘
```

**Implementation**:

```xml
<!-- Spring Boot application.yml -->
spring:
  datasource:
    primary:
      url: jdbc:mysql://master.rds.amazonaws.com:3306/job_db
      username: admin
      password: ${DB_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver
    replica:
      url: jdbc:mysql://replica1.rds.amazonaws.com:3306/job_db
      username: reader
      password: ${DB_READER_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver

jpa:
  hibernate:
    ddl-auto: validate
```

**Read/Write Separation**:

```java
@Service
@Transactional(readOnly = true)
public class JobSearchService {

    @Autowired
    @Qualifier("replicaDataSource")
    private DataSource replicaDataSource;

    // Search queries use replica
    public List<Job> searchJobs(String keyword) {
        // Queries hit read replicas
        return jobRepository.searchByKeyword(keyword);
    }
}

@Service
public class ApplicationService {

    @Autowired
    @Qualifier("primaryDataSource")
    private DataSource primaryDataSource;

    @Transactional(readOnly = false)
    public Application submitApplication(Long userId, Long jobId) {
        // Writes hit master
        return applicationRepository.save(new Application(...));
    }
}
```

**Capacity Increase**: 5,000 req/sec (master) + distributed reads

---

### Phase 3: Database Sharding (500K+ Users)

**Shard Strategy: By User ID**

```
Shard 0: Users 0-99,999 → DB1
Shard 1: Users 100K-199,999 → DB2
Shard 2: Users 200K-299,999 → DB3
...
Shard N: Users (N*100K)-((N+1)*100K) → DBN

Formula: shard_id = user_id % total_shards
```

**Implementation**:

```java
@Service
public class ShardingService {

    private static final int TOTAL_SHARDS = 10;

    /**
     * Determine which shard a user belongs to
     */
    public int getShardId(Long userId) {
        return (int) (userId % TOTAL_SHARDS);
    }

    /**
     * Get datasource for specific shard
     */
    public DataSource getShardDataSource(int shardId) {
        return dataSourceMap.get("shard_" + shardId);
    }

    /**
     * Route query to correct shard
     */
    public User getUserFromShard(Long userId) {
        int shardId = getShardId(userId);
        DataSource ds = getShardDataSource(shardId);
        // Query specific shard database
        return userRepository.findById(userId);
    }
}
```

**Cross-Shard Queries**:

```java
/**
 * Query all shards for global search
 * This is expensive - use caching when possible
 */
public List<Job> searchAllJobs(String keyword) {
    List<Job> results = new ArrayList<>();

    for (int i = 0; i < TOTAL_SHARDS; i++) {
        DataSource shardDs = getShardDataSource(i);
        // Query each shard and combine
        results.addAll(queryShardForJobs(shardDs, keyword));
    }

    return results;
}
```

**Capacity**: 50,000+ req/sec (distributed across shards)

---

## Backend Scaling

### Horizontal Scaling (Multiple Instances)

```yaml
# docker-compose.yml for production
version: "3.8"
services:
  backend-1:
    image: job-platform:latest
    environment:
      - SHARD_ID=0
      - INSTANCE_ID=backend-1
    ports:
      - "8001:8080"
    depends_on:
      - mysql-master
      - redis-cluster

  backend-2:
    image: job-platform:latest
    environment:
      - SHARD_ID=1
      - INSTANCE_ID=backend-2
    ports:
      - "8002:8080"
    depends_on:
      - mysql-master
      - redis-cluster

  backend-3:
    image: job-platform:latest
    environment:
      - SHARD_ID=2
      - INSTANCE_ID=backend-3
    ports:
      - "8003:8080"
    depends_on:
      - mysql-master
      - redis-cluster

  nginx-lb:
    image: nginx:latest
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
```

**Nginx Load Balancer Configuration**:

```nginx
upstream backend {
    least_conn;  # Use least connections algorithm
    server backend-1:8080 max_fails=3 fail_timeout=30s;
    server backend-2:8080 max_fails=3 fail_timeout=30s;
    server backend-3:8080 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name api.jobmatch.com;

    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_buffering off;
        proxy_request_buffering off;
    }
}
```

**Spring Boot Configuration for Load Balancer**:

```yaml
spring:
  application:
    name: job-platform-api

server:
  servlet:
    context-path: /api/v1
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

**Health Check Endpoint** (for load balancer):

```java
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "instance", System.getenv("INSTANCE_ID"),
            "timestamp", System.currentTimeMillis()
        ));
    }
}
```

---

## Frontend Scaling

### Content Delivery Network (CDN)

```
User Request
    ↓
Cloudfront (CDN)
    ├─ Cache Static Assets (React, CSS, JS, Images)
    ├─ Gzip Compression
    └─ TTL: 1 hour for JS, 24 hours for images
    ↓
S3 Bucket (Origin)
```

**AWS CloudFront Configuration**:

```json
{
  "DistributionConfig": {
    "Enabled": true,
    "DefaultRootObject": "index.html",
    "Origins": [
      {
        "Id": "S3Origin",
        "DomainName": "frontend-bucket.s3.amazonaws.com",
        "S3OriginConfig": {
          "OriginAccessIdentity": "origin-access-identity/cloudfront/XXX"
        }
      }
    ],
    "DefaultCacheBehavior": {
      "AllowedMethods": ["GET", "HEAD"],
      "CachePolicyId": "658327ea-f89d-4fab-a63d-7e88639e58f6",
      "Compress": true,
      "ViewerProtocolPolicy": "redirect-to-https"
    }
  }
}
```

**Production Build Optimization**:

```bash
# package.json
{
  "scripts": {
    "build": "vite build --minify esbuild",
    "build:analyze": "vite build --analyze",
    "build:report": "webpack-bundle-analyzer dist/stats.json"
  },
  "dependencies": {
    "react": "^18.0.0",
    "react-router-dom": "^6.0.0",
    "@reduxjs/toolkit": "^1.9.0"
  }
}
```

**Code Splitting**:

```javascript
// React Router with lazy loading
import { lazy, Suspense } from "react";

const JobBrowsePage = lazy(() => import("./pages/JobBrowse"));
const ProfilePage = lazy(() => import("./pages/Profile"));
const AdminDashboard = lazy(() => import("./pages/AdminDashboard"));

function App() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <Routes>
        <Route path="/jobs" element={<JobBrowsePage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/admin" element={<AdminDashboard />} />
      </Routes>
    </Suspense>
  );
}
```

---

## Search Optimization

### Full-Text Search with Elasticsearch

**Why Elasticsearch**:

- MySQL full-text search too slow for millions of jobs
- Faceted search (skills, location, salary ranges)
- Auto-complete suggestions
- Relevance ranking
- 100ms search response time

**Architecture**:

```
Job Posted Event
    ↓
Spring Boot Service
    ├─ Save to MySQL (transactional)
    └─ Index to Elasticsearch (async)
         ↓
    ┌─────────────────────────┐
    │ Elasticsearch Cluster   │
    │ (3 nodes, 100GB each)   │
    └─────────────────────────┘
         ↓
    User Searches
    ├─ Get from Elasticsearch
    └─ Retrieve full data from MySQL cache
```

**Elasticsearch Mapping (Job Index)**:

```json
{
  "mappings": {
    "properties": {
      "jobId": {
        "type": "keyword"
      },
      "title": {
        "type": "text",
        "analyzer": "standard",
        "fields": {
          "keyword": {
            "type": "keyword"
          }
        }
      },
      "description": {
        "type": "text",
        "analyzer": "standard"
      },
      "companyName": {
        "type": "text",
        "analyzer": "standard",
        "fields": {
          "keyword": {
            "type": "keyword"
          }
        }
      },
      "location": {
        "type": "geo_point"
      },
      "skills": {
        "type": "keyword"
      },
      "salaryMin": {
        "type": "long"
      },
      "salaryMax": {
        "type": "long"
      },
      "jobType": {
        "type": "keyword"
      },
      "isRemote": {
        "type": "boolean"
      },
      "postedDate": {
        "type": "date"
      }
    }
  }
}
```

**Spring Boot Integration**:

```java
@Configuration
public class ElasticsearchConfig {

    @Bean
    public RestHighLevelClient client() {
        return new RestHighLevelClient(
            RestClient.builder(
                new HttpHost("elasticsearch-1", 9200, "http"),
                new HttpHost("elasticsearch-2", 9200, "http"),
                new HttpHost("elasticsearch-3", 9200, "http")
            )
        );
    }
}

@Service
public class JobSearchService {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    /**
     * Search jobs with filters
     */
    public SearchResponse searchJobs(String keyword, String location,
                                     Long salaryMin, Long salaryMax) {
        SearchRequest searchRequest = new SearchRequest("jobs");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // Match keyword in title or description
        if (keyword != null && !keyword.isEmpty()) {
            boolQuery.must(
                QueryBuilders.multiMatchQuery(keyword)
                    .field("title", 2.0f)
                    .field("description", 1.0f)
                    .field("companyName", 1.5f)
            );
        }

        // Filter by location
        if (location != null) {
            boolQuery.filter(QueryBuilders.matchQuery("location", location));
        }

        // Filter by salary range
        if (salaryMin != null || salaryMax != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("salaryMin");
            if (salaryMin != null) rangeQuery.gte(salaryMin);
            if (salaryMax != null) rangeQuery.lte(salaryMax);
            boolQuery.filter(rangeQuery);
        }

        sourceBuilder.query(boolQuery);
        sourceBuilder.from(0).size(20); // Pagination
        searchRequest.source(sourceBuilder);

        try {
            return elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch search failed", e);
        }
    }
}
```

---

## Caching Strategy

### Redis Cluster (Distributed Caching)

```
Application Requests
    ↓
Redis Cluster (3-9 nodes)
├─ User profiles (TTL: 1 hour)
├─ Active jobs (TTL: 30 minutes)
├─ Skills catalog (TTL: 24 hours)
├─ Recommendations (TTL: 7 days)
├─ Session tokens (TTL: 24 hours)
└─ Search results (TTL: 30 minutes)
```

**Spring Boot with Redis**:

```yaml
spring:
  redis:
    cluster:
      nodes:
        - redis-node-1:6379
        - redis-node-2:6379
        - redis-node-3:6379
    timeout: 2000
    jedis:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

**Caching Annotations**:

```java
@Service
@CacheConfig(cacheNames = "jobs")
public class JobService {

    @Cacheable(key = "#id", unless = "#result == null")
    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElse(null);
    }

    @CachePut(key = "#job.id")
    public Job updateJob(Job job) {
        return jobRepository.save(job);
    }

    @CacheEvict(key = "#id")
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }
}

@Service
@CacheConfig(cacheNames = "skills")
public class SkillService {

    @Cacheable(unless = "#result == null")
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
}

@Service
public class RecommendationService {

    @Cacheable(key = "#userId + ':recommendations'", cacheNames = "recommendations")
    public List<JobRecommendation> getRecommendations(Long userId) {
        // Expensive computation
        return calculateRecommendations(userId);
    }
}
```

**Cache Invalidation Strategy**:

```java
/**
 * When job posted, invalidate cached recommendations
 */
@Service
public class ApplicationService {

    @Autowired
    private CacheManager cacheManager;

    @Transactional
    public Application submitApplication(Long userId, Long jobId) {
        Application app = applicationRepository.save(
            new Application(userId, jobId)
        );

        // Invalidate user's cached recommendations
        cacheManager.getCache("recommendations")
            .evict(userId + ":recommendations");

        return app;
    }
}
```

---

## Monitoring & Observability

### Application Metrics (Prometheus)

```yaml
# application.yml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: prometheus,health,metrics
  endpoint:
    prometheus:
      enabled: true
```

**Key Metrics to Monitor**:

- HTTP request latency (p50, p95, p99)
- Database query time
- Cache hit rate
- Error rate by endpoint
- Active connections
- Memory/CPU usage

**Grafana Dashboard**:

```json
{
  "dashboard": {
    "title": "Job Platform - Production",
    "panels": [
      {
        "title": "Request Latency",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, http_requests_duration_seconds_bucket)"
          }
        ]
      },
      {
        "title": "Error Rate",
        "targets": [
          {
            "expr": "rate(http_requests_total{status=~'5..'}[5m])"
          }
        ]
      },
      {
        "title": "Cache Hit Rate",
        "targets": [
          {
            "expr": "rate(cache_hits_total[5m]) / rate(cache_requests_total[5m])"
          }
        ]
      }
    ]
  }
}
```

### Distributed Logging

```yaml
# logback-spring.xml
<configuration>
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
<encoder>
<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
</encoder>
</appender>

<!-- Send logs to ELK Stack -->
<appender name="ELASTICSEARCH" class="com.internetitem.logback.elasticsearch.ElasticsearchAppender">
<url>http://elasticsearch:9200</url>
<index>logs-%d{yyyy.MM.dd}</index>
<type>_doc</type>
</appender>

<root level="INFO">
<appender-ref ref="STDOUT" />
<appender-ref ref="ELASTICSEARCH" />
</root>
</configuration>
```

### Alerting

```yaml
# Prometheus alerting rules
groups:
  - name: job_platform
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~'5..'}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate detected"

      - alert: DatabaseSlow
        expr: histogram_quantile(0.95, db_query_duration_seconds) > 1
        for: 5m
        annotations:
          summary: "Database queries too slow"
```

---

## Cost Optimization

### Reserved Instances

```
On-Demand: $0.10/hour per instance
Reserved (1-year): $0.06/hour per instance
Savings: 40% on compute
```

### Auto-Scaling Policies

```json
{
  "AutoScalingGroupName": "job-platform-backend",
  "MinSize": 3,
  "MaxSize": 20,
  "DesiredCapacity": 5,
  "TargetTrackingScaling": {
    "MetricType": "CPUUtilization",
    "TargetValue": 70.0,
    "ScaleOutThreshold": 80,
    "ScaleInThreshold": 50
  }
}
```

### Storage Optimization

- Compress old application records (archive to S3)
- Use S3 lifecycle policies (90 days → Glacier)
- Database cleanup: purge logs older than 90 days
- CDN cache: 24 hours for assets

---

## Summary: Scaling Timeline

| Phase      | Users | Concurrency | Infrastructure                          | Cost    | Timeline |
| ---------- | ----- | ----------- | --------------------------------------- | ------- | -------- |
| MVP        | 5K    | 500         | 1 Backend + 1 MySQL                     | $100/mo | Month 1  |
| Growth     | 50K   | 5K          | 3 Backends + Read Replicas + Redis      | $500/mo | Month 4  |
| Scale      | 500K  | 50K         | 10+ Backends + Sharding + Elasticsearch | $2K/mo  | Month 9  |
| Enterprise | 5M+   | 100K        | Multi-region + Advanced caching         | $5K+/mo | Month 18 |
