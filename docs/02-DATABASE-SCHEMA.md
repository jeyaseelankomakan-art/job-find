# Job Matching Platform - Database Schema

## Table of Contents

1. [Schema Overview](#schema-overview)
2. [ER Diagram Relationships](#er-diagram-relationships)
3. [Core Tables](#core-tables)
4. [Indexes & Performance](#indexes--performance)
5. [Normalization Analysis](#normalization-analysis)

---

## Schema Overview

**Database**: job_matching_db
**Character Set**: utf8mb4
**Collation**: utf8mb4_unicode_ci

---

## ER Diagram Relationships

```
┌──────────────┐
│    Users     │
├──────────────┤
│ id (PK)      │
│ email (UQ)   │────────────┐
│ role         │            │
│ status       │            │
│ created_at   │            │
└──────────────┘            │
       │                    │
       │ (1:M)             │
       ↓                    │
┌──────────────┐            │
│ UserSkills   │            │
├──────────────┤            │
│ id (PK)      │            │
│ user_id (FK) │            │
│ skill_id (FK)│            │
│ level        │            │
└──────────────┘            │
                            │
┌──────────────┐            │
│  Companies   │            │
├──────────────┤            │
│ id (PK)      │────────────┤
│ name (UQ)    │            │
│ status       │            │
│ verified_by  │────────────┤
└──────────────┘            │
       │                    │
       │ (1:M)             │
       ↓                    │
┌──────────────┐            │
│ CompanyAdmins│            │
├──────────────┤            │
│ id (PK)      │            │
│ company_id FK│            │
│ user_id (FK) │────────────┤
└──────────────┘            │
                            │
       ┌────────────────────┘
       │
┌──────────────┐      ┌──────────────┐
│    Jobs      │      │  JobSkills   │
├──────────────┤      ├──────────────┤
│ id (PK)      │◄─────│ job_id (FK)  │
│ company_id FK│      │ skill_id (FK)│
│ title        │      │ level        │
│ status       │      └──────────────┘
│ posted_date  │             ▲
│ salary_min   │             │
│ salary_max   │             │ (M:1)
│ location     │             │
└──────────────┘      ┌──────────────┐
       │              │    Skills    │
       │ (1:M)        ├──────────────┤
       ↓              │ id (PK)      │
┌──────────────┐      │ name (UQ)    │
│Applications  │      │ category     │
├──────────────┤      └──────────────┘
│ id (PK)      │
│ job_id (FK)  │
│ user_id (FK) │
│ status       │
│ match_score  │
│ applied_date │
└──────────────┘
       │ (1:M)
       ↓
┌──────────────────────┐
│ ApplicationHistory   │
├──────────────────────┤
│ id (PK)              │
│ application_id (FK)  │
│ status               │
│ changed_date         │
│ note                 │
└──────────────────────┘

┌──────────────────────┐
│  Notifications       │
├──────────────────────┤
│ id (PK)              │
│ user_id (FK)         │
│ type                 │
│ title                │
│ message              │
│ read_at              │
│ created_at           │
└──────────────────────┘

┌──────────────────────┐
│ ContactRequests      │
├──────────────────────┤
│ id (PK)              │
│ sender_id (FK)       │
│ receiver_id (FK)     │
│ job_id (FK)          │
│ message              │
│ status               │
│ created_at           │
└──────────────────────┘

┌──────────────────────┐
│   AdminLogs          │
├──────────────────────┤
│ id (PK)              │
│ admin_id (FK)        │
│ action_type          │
│ target_entity        │
│ target_id            │
│ details              │
│ timestamp            │
└──────────────────────┘
```

---

## Core Tables

### 1. Users

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('JOB_SEEKER', 'COMPANY_ADMIN', 'SYSTEM_ADMIN') NOT NULL DEFAULT 'JOB_SEEKER',
    status ENUM('ACTIVE', 'INACTIVE', 'BLOCKED', 'PENDING_VERIFICATION') NOT NULL DEFAULT 'ACTIVE',
    profile_picture_url VARCHAR(500),
    bio TEXT,
    location VARCHAR(255),

    -- Job Seeker specific
    years_of_experience INT,
    highest_education ENUM('HIGH_SCHOOL', 'BACHELOR', 'MASTER', 'PHD', 'OTHER'),
    preferred_job_title VARCHAR(255),
    cv_url VARCHAR(500),
    cv_parsed_skills JSON,  -- For future AI parsing

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,

    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. Companies

```sql
CREATE TABLE companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    website VARCHAR(500),
    logo_url VARCHAR(500),
    cover_image_url VARCHAR(500),
    industry VARCHAR(100),
    company_size ENUM('STARTUP', 'SMALL', 'MEDIUM', 'LARGE', 'ENTERPRISE'),
    founded_year INT,
    headquarters_location VARCHAR(255),

    -- Verification
    status ENUM('PENDING', 'VERIFIED', 'REJECTED', 'BLOCKED') NOT NULL DEFAULT 'PENDING',
    verified_by BIGINT,
    verification_date TIMESTAMP NULL,
    rejection_reason TEXT,

    -- Contact
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (verified_by) REFERENCES users(id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. CompanyAdmins

```sql
CREATE TABLE company_admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('OWNER', 'ADMIN', 'MODERATOR') NOT NULL DEFAULT 'ADMIN',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_company_user (company_id, user_id),
    INDEX idx_company_id (company_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4. Jobs

```sql
CREATE TABLE jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description LONGTEXT NOT NULL,
    job_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE'),
    category VARCHAR(100),

    -- Salary
    salary_min DECIMAL(10, 2),
    salary_max DECIMAL(10, 2),
    salary_currency VARCHAR(3) DEFAULT 'USD',

    -- Location
    location VARCHAR(255) NOT NULL,
    is_remote BOOLEAN DEFAULT FALSE,
    office_address TEXT,

    -- Requirements
    experience_level ENUM('ENTRY', 'MID', 'SENIOR', 'LEAD') NOT NULL DEFAULT 'MID',
    years_required INT DEFAULT 0,

    -- Status
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    posted_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closing_date TIMESTAMP NULL,

    -- Metadata
    application_count INT DEFAULT 0,
    view_count INT DEFAULT 0,

    created_by BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_company_id (company_id),
    INDEX idx_status (status),
    INDEX idx_posted_date (posted_date),
    INDEX idx_location (location),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5. JobSkills

```sql
CREATE TABLE job_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') NOT NULL DEFAULT 'INTERMEDIATE',
    is_mandatory BOOLEAN DEFAULT FALSE,

    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE KEY unique_job_skill (job_id, skill_id),
    INDEX idx_job_id (job_id),
    INDEX idx_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 6. Skills

```sql
CREATE TABLE skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),
    description TEXT,
    popularity_score INT DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_name (name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 7. UserSkills

```sql
CREATE TABLE user_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') NOT NULL DEFAULT 'INTERMEDIATE',
    years_of_experience INT DEFAULT 0,
    endorsed_count INT DEFAULT 0,

    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_skill (user_id, skill_id),
    INDEX idx_user_id (user_id),
    INDEX idx_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 8. Applications

```sql
CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status ENUM('APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEWED', 'OFFERED', 'REJECTED', 'ACCEPTED', 'WITHDRAWN') NOT NULL DEFAULT 'APPLIED',

    -- Matching
    match_score DECIMAL(5, 2),  -- 0-100
    skill_match DECIMAL(5, 2),
    experience_match DECIMAL(5, 2),
    location_match DECIMAL(5, 2),

    -- Application Details
    cover_letter TEXT,
    applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_date TIMESTAMP NULL,
    reviewed_by BIGINT,
    rejection_reason TEXT,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id),
    UNIQUE KEY unique_job_user_application (job_id, user_id),
    INDEX idx_job_id (job_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_applied_date (applied_date),
    INDEX idx_match_score (match_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 9. ApplicationHistory

```sql
CREATE TABLE application_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    status ENUM('APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEWED', 'OFFERED', 'REJECTED', 'ACCEPTED', 'WITHDRAWN') NOT NULL,
    changed_by BIGINT,
    note TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id),
    INDEX idx_application_id (application_id),
    INDEX idx_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 10. Notifications

```sql
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type ENUM('APPLICATION_RECEIVED', 'APPLICATION_STATUS_CHANGE', 'JOB_RECOMMENDED', 'COMPANY_VERIFIED', 'MESSAGE_RECEIVED', 'JOB_MATCH_ALERT', 'ADMIN_ALERT') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,

    -- Reference
    related_entity_type VARCHAR(50),  -- 'JOB', 'APPLICATION', 'COMPANY', etc.
    related_entity_id BIGINT,

    -- Status
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 11. ContactRequests

```sql
CREATE TABLE contact_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    job_id BIGINT,
    message TEXT,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED') NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,

    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE SET NULL,
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 12. AdminLogs

```sql
CREATE TABLE admin_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    action_type ENUM('USER_BLOCKED', 'USER_UNBLOCKED', 'COMPANY_VERIFIED', 'COMPANY_REJECTED', 'JOB_REMOVED', 'APPLICATION_REJECTED', 'REPORT_RESOLVED', 'ROLE_CHANGED') NOT NULL,
    target_entity_type VARCHAR(50),  -- 'USER', 'COMPANY', 'JOB', etc.
    target_entity_id BIGINT,
    details JSON,  -- Additional info about the action
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (admin_id) REFERENCES users(id),
    INDEX idx_admin_id (admin_id),
    INDEX idx_action_type (action_type),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Indexes & Performance

### Critical Indexes (Already defined in tables above)

| Table               | Index              | Purpose                 | Query Benefit |
| ------------------- | ------------------ | ----------------------- | ------------- |
| users               | idx_email          | Fast login lookup       | O(1)          |
| users               | idx_role           | Filter by user type     | O(log n)      |
| companies           | idx_status         | Find verified companies | O(log n)      |
| jobs                | idx_company_id     | List company jobs       | O(log n)      |
| jobs                | idx_status         | Find open jobs          | O(log n)      |
| jobs                | idx_posted_date    | Recent jobs             | O(log n)      |
| jobs                | idx_location       | Location-based search   | O(log n)      |
| applications        | idx_job_id         | Find applicants for job | O(log n)      |
| applications        | idx_user_id        | Get user's applications | O(log n)      |
| applications        | idx_status         | Filter applications     | O(log n)      |
| application_history | idx_application_id | Get history             | O(log n)      |
| job_skills          | idx_job_id         | Job requirements        | O(log n)      |
| user_skills         | idx_user_id        | User capabilities       | O(log n)      |
| notifications       | idx_user_id        | Get user notifications  | O(log n)      |

### Composite Indexes (For complex queries)

```sql
-- Find applicants with specific status for a job
CREATE INDEX idx_job_status ON applications(job_id, status);

-- Find user's submitted applications for status
CREATE INDEX idx_user_status ON applications(user_id, status);

-- Find jobs by company and status
CREATE INDEX idx_company_status ON jobs(company_id, status);

-- Find jobs matching criteria (location + type + status)
CREATE INDEX idx_location_type_status ON jobs(location, job_type, status);
```

---

## Normalization Analysis

### First Normal Form (1NF)

✅ **Satisfied**: No repeating groups or arrays (except JSON for future use)

- Each attribute contains atomic values
- Example: job_skills is a separate table, not a comma-separated column

### Second Normal Form (2NF)

✅ **Satisfied**: All non-key attributes depend on entire primary key

- No partial dependencies
- Example: job_title depends on job_id (not partial)
- All compound keys (like job_id + skill_id) have dependent attributes

### Third Normal Form (3NF)

✅ **Satisfied**: No transitive dependencies

- Non-key attributes depend only on primary key, not on other non-key attributes
- Example: In applications table:
  - skill_match, experience_match, location_match depend on (job_id, user_id)
  - Not on each other

### Example: Why job_skills is a separate table (Normalization)

```
❌ BAD (Not normalized):
Jobs table with: job_id, title, skills[]
  Problem: Can't query efficiently, no way to filter by skill

✅ GOOD (Normalized):
Jobs table: job_id, title
JobSkills table: job_id, skill_id, proficiency_level
  Benefit: Can join and filter easily
  Query: SELECT j.* FROM jobs j
         JOIN job_skills js ON j.id = js.job_id
         WHERE js.skill_id = 5
```

---

## Data Integrity Constraints

### Referential Integrity

- **Cascading Deletes**: When company deleted, all its jobs/admins deleted
- **Foreign Key Constraints**: Prevent orphaned records
- **Unique Constraints**: Prevent duplicate entries (email, company name, skills)

### Business Rules (Enforced in Application Layer)

```
1. User can't apply twice to same job
2. Company must be verified before posting jobs
3. Admin can't be deleted while company owner
4. Job can't be closed if still has pending applications
5. User must have at least one skill to get recommendations
```

---

## Scalability Notes

For production at scale (millions of users):

1. **Partition applications table by date** (monthly partitions)
2. **Archive old admin_logs** (>1 year) to separate storage
3. **Use read replicas** for reporting queries
4. **Implement caching** for skills catalog, popular jobs
5. **Elasticsearch index** for job search (title, description, company)
