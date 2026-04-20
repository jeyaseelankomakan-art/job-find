-- Job Matching Platform - Complete Database Schema
-- Database: job_matching_db
-- Character Set: utf8mb4
-- Collation: utf8mb4_unicode_ci

-- Create database
CREATE DATABASE IF NOT EXISTS job_matching_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE job_matching_db;

-- ============================================================
-- 1. USERS TABLE
-- ============================================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE COMMENT 'User email address',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Bcrypt hashed password',
    full_name VARCHAR(255) NOT NULL COMMENT 'Full name of user',
    phone VARCHAR(20) COMMENT 'Contact phone number',
    
    role ENUM('JOB_SEEKER', 'COMPANY_ADMIN', 'SYSTEM_ADMIN') NOT NULL DEFAULT 'JOB_SEEKER' COMMENT 'User role',
    status ENUM('ACTIVE', 'INACTIVE', 'BLOCKED', 'PENDING_VERIFICATION') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Account status',
    
    profile_picture_url VARCHAR(500) COMMENT 'URL to profile picture',
    bio TEXT COMMENT 'User bio/summary',
    location VARCHAR(255) COMMENT 'Current location (city, state)',
    
    -- Job seeker specific fields
    years_of_experience INT COMMENT 'Years of professional experience',
    highest_education ENUM('HIGH_SCHOOL', 'BACHELOR', 'MASTER', 'PHD', 'OTHER') COMMENT 'Highest education level',
    preferred_job_title VARCHAR(255) COMMENT 'Preferred job title',
    cv_url VARCHAR(500) COMMENT 'URL to CV/Resume',
    cv_parsed_skills JSON COMMENT 'Skills extracted from CV (for AI parsing)',
    salary_expectation DECIMAL(10, 2) COMMENT 'Expected salary per annum',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation date',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last profile update',
    last_login_at TIMESTAMP NULL COMMENT 'Last login timestamp',
    
    -- Indexes
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. COMPANIES TABLE
-- ============================================================
CREATE TABLE companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE COMMENT 'Company name',
    description TEXT COMMENT 'Company description',
    website VARCHAR(500) COMMENT 'Company website URL',
    logo_url VARCHAR(500) COMMENT 'Company logo URL',
    cover_image_url VARCHAR(500) COMMENT 'Cover image URL',
    industry VARCHAR(100) COMMENT 'Industry sector',
    company_size ENUM('STARTUP', 'SMALL', 'MEDIUM', 'LARGE', 'ENTERPRISE') COMMENT 'Number of employees',
    founded_year INT COMMENT 'Year company was founded',
    headquarters_location VARCHAR(255) COMMENT 'Headquarters location',
    
    -- Verification workflow
    status ENUM('PENDING', 'VERIFIED', 'REJECTED', 'BLOCKED') NOT NULL DEFAULT 'PENDING' COMMENT 'Verification status',
    verified_by BIGINT COMMENT 'Admin who verified company',
    verification_date TIMESTAMP NULL COMMENT 'Date company was verified',
    rejection_reason TEXT COMMENT 'Reason for rejection if rejected',
    
    -- Contact information
    contact_email VARCHAR(255) COMMENT 'Company contact email',
    contact_phone VARCHAR(20) COMMENT 'Company contact phone',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Company creation date',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update',
    
    -- Constraints
    FOREIGN KEY (verified_by) REFERENCES users(id),
    
    -- Indexes
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. COMPANY_ADMINS TABLE
-- ============================================================
CREATE TABLE company_admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL COMMENT 'Company reference',
    user_id BIGINT NOT NULL COMMENT 'User who is admin',
    role ENUM('OWNER', 'ADMIN', 'MODERATOR') NOT NULL DEFAULT 'ADMIN' COMMENT 'Admin role within company',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Admin status',
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When admin was added',
    
    -- Constraints
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_company_user (company_id, user_id),
    
    -- Indexes
    INDEX idx_company_id (company_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. SKILLS TABLE
-- ============================================================
CREATE TABLE skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Skill name (e.g., Java, React)',
    category VARCHAR(50) COMMENT 'Skill category (e.g., Programming, Framework)',
    description TEXT COMMENT 'Skill description',
    popularity_score INT DEFAULT 0 COMMENT 'Popularity score (0-100)',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When skill was added',
    
    -- Indexes
    INDEX idx_name (name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. USER_SKILLS TABLE (User's Skills)
-- ============================================================
CREATE TABLE user_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'User who has this skill',
    skill_id BIGINT NOT NULL COMMENT 'The skill',
    proficiency_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') NOT NULL DEFAULT 'INTERMEDIATE' COMMENT 'Proficiency level',
    years_of_experience INT DEFAULT 0 COMMENT 'Years of experience with skill',
    endorsed_count INT DEFAULT 0 COMMENT 'Number of endorsements',
    
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When skill was added',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update',
    
    -- Constraints
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_skill (user_id, skill_id),
    
    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. JOBS TABLE
-- ============================================================
CREATE TABLE jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL COMMENT 'Company posting the job',
    title VARCHAR(255) NOT NULL COMMENT 'Job title',
    description LONGTEXT NOT NULL COMMENT 'Job description',
    job_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE') COMMENT 'Type of job',
    category VARCHAR(100) COMMENT 'Job category',
    
    -- Salary information
    salary_min DECIMAL(10, 2) COMMENT 'Minimum salary',
    salary_max DECIMAL(10, 2) COMMENT 'Maximum salary',
    salary_currency VARCHAR(3) DEFAULT 'USD' COMMENT 'Currency (USD, EUR, etc)',
    
    -- Location information
    location VARCHAR(255) NOT NULL COMMENT 'Job location',
    is_remote BOOLEAN DEFAULT FALSE COMMENT 'Is remote work allowed?',
    office_address TEXT COMMENT 'Office address',
    
    -- Requirements
    experience_level ENUM('ENTRY', 'MID', 'SENIOR', 'LEAD') NOT NULL DEFAULT 'MID' COMMENT 'Experience level required',
    years_required INT DEFAULT 0 COMMENT 'Years of experience required',
    
    -- Status and dates
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT 'Job status',
    posted_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When job was posted',
    closing_date TIMESTAMP NULL COMMENT 'Application closing date',
    
    -- Metadata
    application_count INT DEFAULT 0 COMMENT 'Number of applications',
    view_count INT DEFAULT 0 COMMENT 'Number of views',
    
    created_by BIGINT NOT NULL COMMENT 'User who created job',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update',
    
    -- Constraints
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    
    -- Indexes (Critical for search performance)
    INDEX idx_company_id (company_id),
    INDEX idx_status (status),
    INDEX idx_posted_date (posted_date),
    INDEX idx_location (location),
    INDEX idx_category (category),
    INDEX idx_company_status (company_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. JOB_SKILLS TABLE (Skills Required for a Job)
-- ============================================================
CREATE TABLE job_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL COMMENT 'The job',
    skill_id BIGINT NOT NULL COMMENT 'Required skill',
    proficiency_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') NOT NULL DEFAULT 'INTERMEDIATE' COMMENT 'Proficiency level required',
    is_mandatory BOOLEAN DEFAULT FALSE COMMENT 'Is this skill mandatory?',
    
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When added to job',
    
    -- Constraints
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE KEY unique_job_skill (job_id, skill_id),
    
    -- Indexes
    INDEX idx_job_id (job_id),
    INDEX idx_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. APPLICATIONS TABLE
-- ============================================================
CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL COMMENT 'Job applied for',
    user_id BIGINT NOT NULL COMMENT 'User who applied',
    status ENUM('APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEWED', 'OFFERED', 'REJECTED', 'ACCEPTED', 'WITHDRAWN') NOT NULL DEFAULT 'APPLIED' COMMENT 'Application status',
    
    -- Matching scores
    match_score DECIMAL(5, 2) COMMENT 'Overall match percentage (0-100)',
    skill_match DECIMAL(5, 2) COMMENT 'Skill match percentage',
    experience_match DECIMAL(5, 2) COMMENT 'Experience match percentage',
    location_match DECIMAL(5, 2) COMMENT 'Location match percentage',
    
    -- Application details
    cover_letter TEXT COMMENT 'Application cover letter',
    applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When applied',
    reviewed_date TIMESTAMP NULL COMMENT 'When reviewed',
    reviewed_by BIGINT COMMENT 'Admin who reviewed',
    rejection_reason TEXT COMMENT 'Reason for rejection if rejected',
    
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update',
    
    -- Constraints
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id),
    UNIQUE KEY unique_job_user_application (job_id, user_id) COMMENT 'Prevent duplicate applications',
    
    -- Indexes (Critical for filtering)
    INDEX idx_job_id (job_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_applied_date (applied_date),
    INDEX idx_match_score (match_score),
    INDEX idx_job_status (job_id, status),
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9. APPLICATION_HISTORY TABLE
-- ============================================================
CREATE TABLE application_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL COMMENT 'The application',
    status ENUM('APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEWED', 'OFFERED', 'REJECTED', 'ACCEPTED', 'WITHDRAWN') NOT NULL COMMENT 'Status at this time',
    changed_by BIGINT COMMENT 'Admin who made change',
    note TEXT COMMENT 'Note about the change',
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When status changed',
    
    -- Constraints
    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id),
    
    -- Indexes
    INDEX idx_application_id (application_id),
    INDEX idx_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. NOTIFICATIONS TABLE
-- ============================================================
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'User receiving notification',
    type ENUM('APPLICATION_RECEIVED', 'APPLICATION_STATUS_CHANGE', 'JOB_RECOMMENDED', 'COMPANY_VERIFIED', 'MESSAGE_RECEIVED', 'JOB_MATCH_ALERT', 'ADMIN_ALERT') NOT NULL COMMENT 'Notification type',
    title VARCHAR(255) NOT NULL COMMENT 'Notification title',
    message TEXT NOT NULL COMMENT 'Notification message',
    
    -- Reference to related entity
    related_entity_type VARCHAR(50) COMMENT 'Type of related entity (JOB, APPLICATION, etc)',
    related_entity_id BIGINT COMMENT 'ID of related entity',
    
    -- Read status
    is_read BOOLEAN DEFAULT FALSE COMMENT 'Has user read this?',
    read_at TIMESTAMP NULL COMMENT 'When user read it',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When notification created',
    
    -- Constraints
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_user_is_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 11. CONTACT_REQUESTS TABLE
-- ============================================================
CREATE TABLE contact_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL COMMENT 'User sending request',
    receiver_id BIGINT NOT NULL COMMENT 'User receiving request',
    job_id BIGINT COMMENT 'Related job (optional)',
    message TEXT COMMENT 'Message from sender',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED') NOT NULL DEFAULT 'PENDING' COMMENT 'Request status',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When request sent',
    responded_at TIMESTAMP NULL COMMENT 'When receiver responded',
    
    -- Constraints
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE SET NULL,
    
    -- Indexes
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 12. ADMIN_LOGS TABLE
-- ============================================================
CREATE TABLE admin_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL COMMENT 'Admin who performed action',
    action_type ENUM('USER_BLOCKED', 'USER_UNBLOCKED', 'COMPANY_VERIFIED', 'COMPANY_REJECTED', 'JOB_REMOVED', 'APPLICATION_REJECTED', 'REPORT_RESOLVED', 'ROLE_CHANGED') NOT NULL COMMENT 'Type of action',
    target_entity_type VARCHAR(50) COMMENT 'Entity type affected (USER, COMPANY, JOB, etc)',
    target_entity_id BIGINT COMMENT 'ID of entity affected',
    details JSON COMMENT 'Additional details about action',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When action was performed',
    
    -- Constraints
    FOREIGN KEY (admin_id) REFERENCES users(id),
    
    -- Indexes
    INDEX idx_admin_id (admin_id),
    INDEX idx_action_type (action_type),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SEED DATA
-- ============================================================

-- Insert common skills
INSERT INTO skills (name, category, description, popularity_score) VALUES
('Java', 'Programming Language', 'Enterprise Java programming', 95),
('Python', 'Programming Language', 'General purpose programming', 92),
('JavaScript', 'Programming Language', 'Web development', 95),
('React', 'Frontend Framework', 'React.js framework', 93),
('Spring Boot', 'Backend Framework', 'Spring Boot framework', 88),
('MySQL', 'Database', 'Relational database management', 85),
('SQL', 'Query Language', 'SQL query language', 90),
('REST API', 'Architecture', 'REST API design', 87),
('Docker', 'DevOps', 'Container technology', 82),
('AWS', 'Cloud', 'Amazon Web Services', 84),
('Git', 'Version Control', 'Git version control', 93),
('Node.js', 'Runtime', 'Node.js runtime', 86),
('TypeScript', 'Programming Language', 'TypeScript language', 85),
('Angular', 'Frontend Framework', 'Angular framework', 78),
('MongoDB', 'Database', 'NoSQL database', 76),
('Kubernetes', 'DevOps', 'Container orchestration', 75),
('CSS', 'Frontend', 'CSS styling', 89),
('HTML', 'Frontend', 'HTML markup', 91),
('Linux', 'OS', 'Linux operating system', 79),
('Jenkins', 'CI/CD', 'CI/CD tool', 74);

-- Verify tables were created
SHOW TABLES;

-- Display schema summary
SELECT 
    TABLE_NAME, 
    TABLE_ROWS, 
    DATA_LENGTH,
    COLUMN_COUNT
FROM (
    SELECT 
        t.TABLE_NAME,
        t.TABLE_ROWS,
        t.DATA_LENGTH,
        COUNT(c.COLUMN_NAME) as COLUMN_COUNT
    FROM INFORMATION_SCHEMA.TABLES t
    LEFT JOIN INFORMATION_SCHEMA.COLUMNS c 
        ON t.TABLE_NAME = c.TABLE_NAME 
        AND t.TABLE_SCHEMA = c.TABLE_SCHEMA
    WHERE t.TABLE_SCHEMA = 'job_matching_db'
    GROUP BY t.TABLE_NAME
) summary
ORDER BY TABLE_NAME;
