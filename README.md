# Job Matching & Career Connection Platform

## Complete Production-Level System Design

**Project Status**: Full Documentation & MVP Roadmap Complete
**Last Updated**: April 2026
**Team Size**: 1 developer

---

## 📋 Project Overview

A comprehensive web platform connecting **job seekers** with **employers** through intelligent **skill-based matching**. Features include:

✅ Job seeker profiles with CV upload and skill management
✅ Company registration with verification workflow
✅ Job posting with skill requirements
✅ Smart matching engine (0-100% compatibility score)
✅ Job applications with status tracking
✅ Admin moderation and analytics dashboard
✅ Real-time notifications
✅ Direct messaging between users and companies

---

## 📁 Project Structure

```
job/
├── docs/
│   ├── 01-SYSTEM-ARCHITECTURE.md       # Complete architecture overview
│   ├── 02-DATABASE-SCHEMA.md           # Full database design with SQL
│   ├── 03-API-SPECIFICATION.md         # All REST API endpoints (70+ endpoints)
│   ├── 04-MATCHING-ENGINE.md           # Matching algorithm with Java code
│   ├── 05-MVP-ROADMAP.md               # 16-week development plan
│   └── 06-SCALING-STRATEGY.md          # Production scaling for millions of users
│
├── database/
│   ├── 01-SCHEMA.sql                   # Complete database schema
│   └── migrations/                     # Flyway migration scripts (future)
│
├── backend/
│   ├── pom.xml                         # Maven dependencies (Spring Boot 3.1)
│   ├── src/main/
│   │   ├── java/com/jobmatch/api/
│   │   │   ├── JobMatchingPlatformApplication.java    # Main app
│   │   │   ├── controller/             # REST controllers
│   │   │   ├── service/                # Business logic
│   │   │   ├── repository/             # Data access (JPA)
│   │   │   ├── model/
│   │   │   │   ├── entity/             # JPA entities
│   │   │   │   └── dto/                # Data transfer objects
│   │   │   ├── config/                 # Spring configuration
│   │   │   ├── security/               # JWT & authentication
│   │   │   └── exception/              # Custom exceptions
│   │   └── resources/
│   │       ├── application.yml         # Configuration
│   │       └── db/migration/           # Flyway migrations
│   └── src/test/java/                  # Unit & integration tests
│
├── frontend/
│   ├── package.json                    # Node dependencies (React 18)
│   ├── vite.config.js                  # Vite configuration
│   ├── tailwind.config.js              # Tailwind configuration
│   └── src/
│       ├── App.jsx                     # Main app component
│       ├── index.css                   # Tailwind imports
│       ├── components/                 # Reusable components
│       ├── pages/                      # Page components
│       ├── services/                   # API client services
│       ├── hooks/                      # Custom React hooks
│       └── context/                    # React context (auth, etc)
│
└── README.md (this file)
```

---

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- MySQL 8.0+
- Docker & Docker Compose (optional)

### Backend Setup

```bash
cd backend

# Install dependencies
mvn clean install

# Run database schema
mysql -u root -p < ../database/01-SCHEMA.sql

# Start Spring Boot server
mvn spring-boot:run

# Server runs at: http://localhost:8080/api/v1
```

### Database Profiles (Local vs Docker)

The backend now uses profile-based datasource config:

- `local` profile (default): connects to local MySQL/WAMP on `localhost:3306`
- `docker` profile: connects to Docker MySQL service `mysql:3306`

Config files:

- `backend/src/main/resources/application.yml` (shared settings + active profile)
- `backend/src/main/resources/application-local.yml` (local datasource)
- `backend/src/main/resources/application-docker.yml` (docker datasource)

Run backend with local profile (default):

```bash
cd backend
mvn spring-boot:run
```

Run backend with docker profile:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

Override local DB credentials without editing files (PowerShell example):

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/job_matching_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD=""
mvn spring-boot:run
```

### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Frontend runs at: http://localhost:5173
```

### Docker Setup (Recommended)

```bash
# From project root
docker-compose up

# Services:
# - MySQL: localhost:3306
# - Backend: localhost:8080
# - Frontend: localhost:3000
```

---

## 📚 Documentation Files

All comprehensive documentation is in the `docs/` folder:

### 1. **System Architecture** (`01-SYSTEM-ARCHITECTURE.md`)

- High-level architecture overview
- Component descriptions
- Request flow examples
- Technology stack details
- Security architecture
- Performance & scalability considerations

**Read this first to understand the entire system.**

### 2. **Database Schema** (`02-DATABASE-SCHEMA.md`)

- Complete SQL schema for all 12 tables
- ER diagram relationships
- Indexing strategy
- Normalization analysis (1NF, 2NF, 3NF)
- Data integrity constraints

**Use this to understand data model and set up the database.**

### 3. **API Specification** (`03-API-SPECIFICATION.md`)

- 70+ REST API endpoints
- Request/response examples for each
- Query parameters and filters
- Error handling
- Authentication requirements

**Reference this while building frontend or testing backend.**

### 4. **Matching Engine** (`04-MATCHING-ENGINE.md`)

- Detailed matching algorithm logic
- Skill match calculation (40% weight)
- Experience match calculation (30% weight)
- Location match calculation (15% weight)
- Salary match calculation (10% weight)
- Complete Java implementation with code examples
- Recommendations engine
- Future ML enhancements

**Study this to understand how matching works and implement it.**

### 5. **MVP Roadmap** (`05-MVP-ROADMAP.md`)

- **16-week development timeline**
- Phase-by-phase breakdown:
  - Week 1-2: Foundation & Authentication
  - Week 3-4: User profiles & skills
  - Week 5-6: Job posting & search
  - Week 7-9: Applications & matching
  - Week 10-11: Admin & notifications
  - Week 12-13: Contact requests & polish
  - Week 14-16: Testing & deployment
- Testing strategy
- Risk mitigation
- Success metrics

**Use this to plan sprints and track progress.**

### 6. **Scaling Strategy** (`06-SCALING-STRATEGY.md`)

- Current MVP architecture (5K users)
- Phase 1 (50K users): Read replicas, caching
- Phase 2 (500K users): Horizontal scaling, Elasticsearch
- Phase 3 (5M users): Database sharding, microservices
- Caching strategy (Redis)
- Search optimization (Elasticsearch)
- Monitoring & observability
- Cost optimization

**Read this to plan for growth from MVP to enterprise scale.**

---

## 🏗️ Technology Stack

### Backend

- **Framework**: Spring Boot 3.1
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Hibernate (JPA)
- **Authentication**: JWT + Spring Security
- **Build Tool**: Maven
- **Database Migrations**: Flyway
- **Caching**: Redis (Phase 2)
- **Search**: Elasticsearch (Phase 2)

### Frontend

- **Framework**: React 18
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **State Management**: Redux Toolkit
- **HTTP Client**: Axios
- **Routing**: React Router v6
- **Form Validation**: Formik + Yup

### DevOps

- **Containerization**: Docker
- **Orchestration**: Docker Compose (local), Kubernetes (prod)
- **CI/CD**: GitHub Actions
- **Cloud**: AWS (RDS, EC2, S3, CloudFront, ALB)

---

## 🔐 Security Features

✅ **Authentication**: JWT-based with 24-hour expiration
✅ **Authorization**: Role-based access control (RBAC)
✅ **Password Security**: Bcrypt hashing with salt
✅ **API Security**: Rate limiting, CORS, HTTPS/TLS
✅ **Data Protection**: Parameterized queries (SQL injection prevention)
✅ **XSS Prevention**: React auto-escapes, Content Security Policy

---

## 📊 Database Schema Summary

### 12 Core Tables

1. **users** - Job seekers and admins (50+ fields)
2. **companies** - Employer accounts
3. **company_admins** - Company ownership/roles
4. **jobs** - Job postings (40+ fields)
5. **job_skills** - Skills required per job
6. **skills** - Skill catalog (100+)
7. **user_skills** - User's skills
8. **applications** - Job applications with match scores
9. **application_history** - Application status changelog
10. **notifications** - User notifications
11. **contact_requests** - Direct messaging
12. **admin_logs** - Audit trail

**Total Fields**: 200+
**Indexes**: 25+ for optimal query performance
**Normalization**: 3NF compliant

---

## 🎯 API Endpoints (Summary)

### Authentication (6 endpoints)

- `POST /auth/register` - Create account
- `POST /auth/login` - Get JWT token
- `POST /auth/logout` - Invalidate token
- `POST /auth/refresh-token` - Refresh token
- `POST /auth/forgot-password` - Request reset
- `POST /auth/reset-password` - Reset password

### Users (10 endpoints)

- `GET /users/profile` - Get profile
- `PUT /users/profile` - Update profile
- `POST /users/profile/cv` - Upload CV
- `GET /users/skills` - List skills
- `POST /users/skills` - Add skill
- `PUT /users/skills/{id}` - Update skill
- `DELETE /users/skills/{id}` - Remove skill
- etc.

### Jobs (8 endpoints)

- `POST /jobs` - Create job
- `GET /jobs` - Search/list jobs (with 10+ filters)
- `GET /jobs/{id}` - Job details
- `PUT /jobs/{id}` - Update job
- `PATCH /jobs/{id}/publish` - Publish job
- `PATCH /jobs/{id}/close` - Close job
- etc.

### Applications (8 endpoints)

- `POST /applications/apply` - Apply for job
- `GET /applications` - User's applications
- `GET /applications/{id}` - Application details
- `PATCH /applications/{id}/withdraw` - Withdraw
- `GET /jobs/{jobId}/applicants` - View applicants (company)
- `PATCH /applications/{id}/status` - Update status (company)
- `PATCH /applications/{id}/reject` - Reject (company)
- etc.

### Recommendations (3 endpoints)

- `GET /recommendations/jobs` - Recommended jobs
- `GET /recommendations/candidates` - Recommended candidates
- `GET /recommendations/skill-gap` - Skill gap analysis

### Admin (8 endpoints)

- `GET /admin/users` - List all users
- `PATCH /admin/users/{id}/block` - Block user
- `PATCH /admin/users/{id}/unblock` - Unblock user
- `GET /admin/companies` - List companies
- `PATCH /admin/companies/{id}/verify` - Verify company
- `PATCH /admin/companies/{id}/reject` - Reject company
- `GET /admin/analytics` - Platform analytics
- `GET /admin/logs` - Admin activity logs

**Total**: 70+ endpoints documented with full examples

---

## 🧠 Matching Algorithm (Simple Rule-Based)

```
Match Score = (Skill Match × 0.40) +
              (Experience Match × 0.30) +
              (Location Match × 0.15) +
              (Salary Match × 0.10) +
              (Bonus Factors × 0.05)

Result: 0-100% match score
Threshold: >= 55% = qualified candidate
```

**Example**:

- Job requires: Java (ADVANCED), Spring Boot (INTERMEDIATE), MySQL (INTERMEDIATE)
- User has: Java (EXPERT), Spring Boot (ADVANCED), Docker (ADVANCED)
- Skill Match: 66.7% (has 2 of 3 required skills)
- Experience: 8 years (job requires 5) = 100%
- Location: SF to SF = 100%
- Salary: $150K in $120K-$160K range = 100%
- **Overall Score: 85.5%** ✅ Highly recommended

---

## 📈 Scaling Plan

### MVP (Months 1-3)

- Single backend instance
- Single MySQL database
- ~5K concurrent users
- Cost: $100-300/month

### Growth Phase (Months 4-8)

- 3+ backend instances behind load balancer
- Read replicas for MySQL
- Redis caching layer
- Elasticsearch for job search
- ~50K concurrent users
- Cost: $500-1,500/month

### Enterprise Phase (Months 9-18)

- Database sharding (by user ID)
- Microservices decomposition
- Multi-region deployment
- Advanced caching & CDN
- ~100K+ concurrent users
- Cost: $2,000-5,000/month

---

## 🧪 Testing Strategy

### Backend Testing (85%+ coverage target)

- Unit tests for services (JUnit 5)
- Integration tests for API endpoints
- Database tests with TestContainers
- Security tests for authentication/authorization
- Performance tests (load testing with JMeter)

### Frontend Testing

- Component tests (React Testing Library)
- E2E tests for main flows (Cypress)
- Browser compatibility testing
- Mobile responsiveness testing

### QA Checklist

- [ ] User registration → Login → Job search → Apply → Notifications
- [ ] Company registration → Job posting → View applicants
- [ ] Admin verification → User blocking → Analytics dashboard
- [ ] Matching algorithm accuracy
- [ ] Performance under load (100 concurrent users)
- [ ] Security: JWT validation, SQL injection prevention

---

## 🚀 Deployment

### Development

```bash
docker-compose up
```

### Production

```bash
# Build Docker images
docker build -t job-platform-backend:latest ./backend
docker build -t job-platform-frontend:latest ./frontend

# Push to registry
docker push your-registry/job-platform-backend:latest
docker push your-registry/job-platform-frontend:latest

# Deploy with Kubernetes or ECS
kubectl apply -f k8s/deployment.yaml
```

---

## 📊 MVP Success Metrics

| Metric                   | Target        |
| ------------------------ | ------------- |
| Code Coverage            | ≥ 80%         |
| API Response Time        | < 500ms (p95) |
| Page Load Time           | < 2 seconds   |
| Uptime                   | ≥ 99.5%       |
| Beta Users               | 100+          |
| Active Job Posts         | 50+           |
| Applications Submitted   | 100+          |
| Admin Verified Companies | 100%          |

---

## 🔗 Key Links & References

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **React Docs**: https://react.dev
- **MySQL Docs**: https://dev.mysql.com/doc/
- **JWT Best Practices**: https://tools.ietf.org/html/rfc7519
- **REST API Design**: https://restfulapi.net
- **System Design**: https://www.educative.io/blog/system-design-interview

---

## 👥 Team Setup Recommendation

### For MVP (16 weeks)

- **2 Backend Engineers**: Spring Boot, database, API design
- **2 Frontend Engineers**: React, UI/UX, responsive design
- **1 DevOps Engineer**: Docker, CI/CD, deployment
- **1 QA Engineer**: Testing, quality assurance

### Suggested Sprint Structure

- **Week 1-2**: Sprint 1 (Foundation)
- **Week 3-4**: Sprint 2 (User Management)
- **Week 5-6**: Sprint 3 (Jobs & Search)
- **Week 7-9**: Sprint 4 (Applications & Matching)
- **Week 10-11**: Sprint 5 (Admin & Notifications)
- **Week 12-13**: Sprint 6 (Polish & Optimization)
- **Week 14-16**: Sprint 7 (Testing & Launch)

---

## 📝 Development Notes

### Important Considerations

1. **Database Connections**: Use connection pooling (HikariCP)
2. **Caching**: Implement Redis for user profiles, job listings
3. **Search**: Use Elasticsearch for full-text job search
4. **Authentication**: JWT tokens stored in httpOnly cookies
5. **File Upload**: Store CVs in S3, not in database
6. **API Rate Limiting**: Prevent abuse
7. **Monitoring**: Use Prometheus + Grafana
8. **Logging**: Centralized logging (ELK stack)

### Common Pitfalls to Avoid

❌ Storing passwords in plain text
❌ N+1 query problems (use JPA eager loading carefully)
❌ Not validating input on both frontend and backend
❌ Storing large files in database
❌ Missing indexes on frequently filtered columns
❌ Not handling concurrent application submissions
❌ Insufficient error handling

---

## 🎓 Learning Path

If you're new to any technology:

1. **Spring Boot**:
   - Official tutorials: https://spring.io/guides
   - Recommended course: Spring Boot by Udemy

2. **React**:
   - Official docs: https://react.dev
   - Tutorial: React documentation interactive tutorial

3. **MySQL**:
   - Official docs: https://dev.mysql.com/doc/
   - Recommended course: MySQL fundamentals

4. **System Design**:
   - Book: "Designing Data-Intensive Applications"
   - Course: "Grokking System Design"

---

## 📄 License

This project design is provided as-is for educational and commercial use.

---

## 🤝 Support

For questions or clarifications:

- Review the comprehensive documentation in `/docs/`
- Check the API specification for endpoint details
- Refer to the MVP roadmap for development timeline
- Consult the matching engine document for algorithm details

---

## ✅ Checklist for Getting Started

- [ ] Read `01-SYSTEM-ARCHITECTURE.md`
- [ ] Review `02-DATABASE-SCHEMA.md`
- [ ] Study `03-API-SPECIFICATION.md`
- [ ] Understand `04-MATCHING-ENGINE.md`
- [ ] Plan sprints using `05-MVP-ROADMAP.md`
- [ ] Consider `06-SCALING-STRATEGY.md` for production
- [ ] Set up development environment (Docker Compose)
- [ ] Create project management board (Jira/GitHub Projects)
- [ ] Establish CI/CD pipeline
- [ ] Begin Sprint 1 (Foundation)

---

**Ready to build? Start with `/docs/01-SYSTEM-ARCHITECTURE.md` →**
