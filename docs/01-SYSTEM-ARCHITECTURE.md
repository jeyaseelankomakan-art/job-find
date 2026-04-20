# Job Matching & Career Connection Platform - System Architecture

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [System Components](#system-components)
3. [Request Flow](#request-flow)
4. [Technology Stack](#technology-stack)
5. [Security Architecture](#security-architecture)
6. [Performance & Scalability](#performance--scalability)

---

## Architecture Overview

### High-Level Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     CLIENT LAYER (React)                        │
│  Job Seeker UI │ Company UI │ Admin Dashboard │ Responsive      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    API GATEWAY & AUTH                           │
│  REST Endpoints │ JWT Validation │ Rate Limiting │ CORS        │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   SERVICE LAYER (Spring Boot)                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Auth Service │  │ User Service │  │ Job Service  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │Company Svc   │  │ Apply Service│  │ Admin Svc    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │Matching Svc  │  │ Notif. Svc   │  │ Search Svc   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│              DATA ACCESS LAYER (JPA/Repositories)              │
│  Transaction Management │ Query Optimization │ Caching        │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER (MySQL)                       │
│  Users │ Companies │ Jobs │ Applications │ Skills │ Logs        │
└─────────────────────────────────────────────────────────────────┘
```

---

## System Components

### 1. **Frontend Layer (React)**

- **SPA Architecture** with React Router for navigation
- **Component-based UI** for reusability
- **State Management**: Redux/Context API
- **Styling**: Tailwind CSS for responsive design
- **Module Distribution**:
  - `auth/` - Login, Register, Password Reset
  - `seeker/` - Job search, Profile, Applications
  - `company/` - Job posting, Applicants, Company Profile
  - `admin/` - Dashboard, Moderation, Analytics
  - `shared/` - Navigation, Layouts, Common Components

### 2. **Backend Layer (Spring Boot)**

#### Authentication & Security

- **JWT-based** authentication
- **OAuth 2.0** ready for social login
- **Role-based Access Control (RBAC)** with `@PreAuthorize`
- **Spring Security** framework

#### Service Layer (Domain-Driven)

- **UserService**: Registration, Profile management, Password reset
- **JobService**: Job CRUD, Job posting workflow
- **ApplicationService**: Job applications, Status tracking
- **CompanyService**: Company profile, Verification workflow
- **MatchingService**: Job-user matching algorithm
- **NotificationService**: Email/Push notifications
- **AdminService**: User moderation, Analytics
- **SearchService**: Elasticsearch integration (for production)

#### Data Layer (JPA)

- **Repository Pattern** for data access
- **Transaction Management** via Spring @Transactional
- **Query Optimization** with projections
- **Lazy/Eager Loading** strategies

### 3. **Database Layer (MySQL)**

**Core Tables**:

- `users` - Job seekers
- `companies` - Employer accounts
- `company_admins` - Company ownership/roles
- `jobs` - Job postings
- `job_skills` - Skills required per job
- `applications` - Job applications
- `application_history` - Status tracking
- `skills` - Skill catalog
- `user_skills` - User's skills
- `notifications` - User notifications
- `contact_requests` - Direct messaging requests
- `admin_logs` - Admin activity logging
- `company_verification` - Company verification workflow

**Relationships**:

```
Users (1) ──→ (M) Applications
          ──→ (M) UserSkills
          ──→ (M) Notifications
          ──→ (M) ContactRequests

Companies (1) ──→ (M) Jobs
           ──→ (M) CompanyAdmins
           ──→ (M) CompanyVerification

Jobs (1) ──→ (M) Applications
       ──→ (M) JobSkills
       ──→ (M) ContactRequests

Skills (1) ──→ (M) UserSkills
        ──→ (M) JobSkills
```

### 4. **Matching Engine**

- **Skill Match**: Calculate overlap between job requirements and user skills
- **Experience Match**: Compare years of experience
- **Location Match**: Geographic proximity (exact/remote)
- **Salary Match**: Expected vs. offered salary alignment
- **Weighted Scoring Algorithm**: Combine all factors into 0-100 score

### 5. **Notification System**

- **In-app notifications** (stored in DB)
- **Email notifications** (async via job queue)
- **Future**: SMS, Push notifications
- **Event-driven**: Application received, Job recommended, etc.

### 6. **Admin Module**

- **User Management**: Block/unblock, view profile
- **Company Verification**: Approve/reject with comments
- **Job Moderation**: Remove spam, verify job posts
- **Analytics Dashboard**: Active users, jobs, applications
- **Activity Logs**: Track all admin actions
- **Reports Handling**: User complaints, flagged content

---

## Request Flow

### Example: Job Seeker Applies for Job

```
1. Frontend: User clicks "Apply" button
   └─ React sends POST /api/applications/apply with {userId, jobId}

2. API Gateway: Validates JWT token, CORS, Rate limiting
   └─ Routes to ApplicationController.apply()

3. Authentication: Spring Security validates user role
   └─ Must be JOB_SEEKER role

4. Service Layer: ApplicationService.createApplication()
   ├─ Verify user exists & is active
   ├─ Verify job exists & is open
   ├─ Check if already applied (prevent duplicates)
   ├─ Create Application entity
   └─ Save to database

5. Data Layer: JPA saves application record
   ├─ INSERT into applications table
   ├─ INSERT into application_history (status: APPLIED)
   └─ Auto-commit transaction

6. Notification: Trigger async notification
   ├─ Send company: "New applicant: John Doe"
   ├─ Send user: "Application submitted"
   └─ Store in notifications table

7. Response: Return 201 Created
   └─ Frontend: Show success toast, redirect

8. Matching Engine (background):
   ├─ Calculate match score
   ├─ Store in applications.match_score
   └─ Update both parties' recommendations
```

### Example: Admin Views Analytics

```
1. Frontend: Admin clicks "Analytics" dashboard
   └─ React sends GET /api/admin/analytics?period=7days

2. API Gateway: Validates admin JWT
   └─ Routes to AdminController.getAnalytics()

3. Authorization: Spring Security checks ADMIN role
   └─ Throws 403 if not admin

4. Service Layer: AdminService.getAnalyticsData()
   ├─ Query count of active users (WHERE status='ACTIVE')
   ├─ Query count of active jobs
   ├─ Query count of applications (this period)
   ├─ Query count of new companies
   └─ Aggregate all metrics

5. Database: Runs optimized COUNT queries with indexes
   └─ Returns results in milliseconds

6. Response: Return JSON with analytics data
   └─ Frontend: Render charts/graphs with data
```

---

## Technology Stack

| Layer        | Technology             | Purpose                      |
| ------------ | ---------------------- | ---------------------------- |
| **Frontend** | React 18               | UI framework                 |
|              | React Router           | Client-side routing          |
|              | Redux/Context          | State management             |
|              | Tailwind CSS           | Styling                      |
|              | Axios/Fetch            | HTTP client                  |
| **Backend**  | Spring Boot 3.x        | REST API framework           |
|              | Spring Security        | Authentication/Authorization |
|              | JPA/Hibernate          | ORM                          |
|              | Maven                  | Build tool                   |
|              | JWT                    | Token-based auth             |
| **Database** | MySQL 8.0              | Relational DB                |
|              | Redis (future)         | Caching layer                |
|              | Elasticsearch (future) | Full-text search             |
| **DevOps**   | Docker                 | Containerization             |
|              | Docker Compose         | Local development            |
|              | GitHub Actions         | CI/CD                        |

---

## Security Architecture

### Authentication Flow

```
1. User submits credentials (email, password)
   ↓
2. Backend validates against password hash (bcrypt)
   ↓
3. If valid: Generate JWT token
   ├─ Header: {typ: JWT, alg: HS256}
   ├─ Payload: {userId, email, role, exp: now+24h}
   ├─ Signature: HMAC-SHA256(header.payload, secret)
   ↓
4. Return token to frontend
   ↓
5. Frontend stores in httpOnly cookie (not localStorage!)
   ↓
6. Every request includes Authorization: Bearer <token>
   ↓
7. Backend validates signature & expiration
   └─ Invalid/expired = 401 Unauthorized
```

### Authorization (RBAC)

```
Roles:
- JOB_SEEKER: Browse jobs, Apply, View profile
- COMPANY_ADMIN: Post jobs, View applicants, Manage company
- COMPANY_MODERATOR: Moderate applicants (sub-role)
- SYSTEM_ADMIN: Full platform control

Spring Security Annotations:
@PreAuthorize("hasRole('JOB_SEEKER')")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
```

### Data Protection

- **Passwords**: Bcrypt hashing with salt
- **API Key**: Encrypt sensitive data at rest
- **HTTPS/TLS**: All communication encrypted
- **SQL Injection**: Use parameterized queries (JPA prevents this)
- **XSS Protection**: React auto-escapes, CSP headers
- **CORS**: Whitelist allowed origins
- **Rate Limiting**: Prevent brute force attacks (Redis-backed)

---

## Performance & Scalability

### Database Optimization

```sql
-- Key Indexes (see 02-DATABASE-SCHEMA.md)
CREATE INDEX idx_user_email ON users(email); -- Fast login
CREATE INDEX idx_job_company ON jobs(company_id); -- List company jobs
CREATE INDEX idx_app_user ON applications(user_id); -- User's applications
CREATE INDEX idx_app_status ON applications(status); -- Filter by status
CREATE INDEX idx_job_skills ON job_skills(job_id); -- Job requirements
CREATE INDEX idx_user_skills ON user_skills(user_id); -- User capabilities
```

### Caching Strategy (Redis)

```
- User profiles: Cache for 1 hour
- Active jobs list: Cache for 30 minutes
- Skills catalog: Cache for 24 hours
- Auth tokens: Cache blacklist for logout
- Matching scores: Cache for 7 days
```

### Search Optimization

```
- Use Elasticsearch for full-text job search
- Index: job title, description, company name, location
- Faceted search: Skills, salary range, job type
- Auto-complete: Job titles, skills, company names
```

### Horizontal Scaling

```
Load Balancer (Nginx)
    ├─ Backend Node 1 (Spring Boot)
    ├─ Backend Node 2 (Spring Boot)
    ├─ Backend Node 3 (Spring Boot)
    └─ Backend Node N (Spring Boot)

Shared Resources:
    ├─ MySQL (Master-Slave replication)
    ├─ Redis Cluster (for sessions/cache)
    └─ S3/Object Storage (for CV uploads)
```

### Async Processing

```
- Email notifications: Job queue (RabbitMQ/Kafka)
- Matching algorithm: Background job (every night)
- CV parsing: Async task (future: AI parsing)
- Analytics aggregation: Scheduled batch job
```

---

## Deployment Architecture

### Development

```
docker-compose up
├─ MySQL (localhost:3306)
├─ Spring Boot Backend (localhost:8080)
└─ React Frontend (localhost:3000)
```

### Production

```
AWS (or similar cloud)
├─ RDS MySQL (managed database)
├─ ECS/EKS (Spring Boot containers)
├─ CloudFront (CDN for React assets)
├─ ElastiCache (Redis)
├─ S3 (CV/Document storage)
├─ CloudWatch (Monitoring)
└─ Route53 (DNS)
```

---

## Next Steps

See the following documents for detailed implementation:

- [02-DATABASE-SCHEMA.md](02-DATABASE-SCHEMA.md) - Full SQL schema
- [03-API-SPECIFICATION.md](03-API-SPECIFICATION.md) - REST API endpoints
- [04-MATCHING-ENGINE.md](04-MATCHING-ENGINE.md) - Matching algorithm logic
- [05-MVP-ROADMAP.md](05-MVP-ROADMAP.md) - Development roadmap
- [06-SCALING-STRATEGY.md](06-SCALING-STRATEGY.md) - Production scaling
