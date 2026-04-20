# Job Matching Platform - REST API Specification

## Base URL

```
Development: http://localhost:8080/api/v1
Production: https://api.jobmatch.com/api/v1
```

## Authentication

All endpoints (except auth endpoints) require JWT token in header:

```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

---

## Authentication Endpoints

### 1. User Registration

```http
POST /auth/register
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "role": "JOB_SEEKER", // or "COMPANY_ADMIN"
  "phone": "+1-234-567-8900"
}

Response: 201 Created
{
  "userId": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "role": "JOB_SEEKER",
  "createdAt": "2024-01-15T10:30:00Z"
}

Error: 409 Conflict (email already exists)
{
  "error": "Email already registered",
  "code": "EMAIL_EXISTS"
}
```

### 2. User Login

```http
POST /auth/login
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400, // seconds (24 hours)
  "user": {
    "userId": 1,
    "email": "user@example.com",
    "role": "JOB_SEEKER",
    "fullName": "John Doe"
  }
}

Error: 401 Unauthorized (invalid credentials)
{
  "error": "Invalid email or password",
  "code": "AUTH_FAILED"
}
```

### 3. Logout

```http
POST /auth/logout
Authorization: Bearer <token>

Response: 200 OK
{
  "message": "Logged out successfully"
}
```

### 4. Refresh Token

```http
POST /auth/refresh-token
Authorization: Bearer <token>

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

### 5. Password Reset Request

```http
POST /auth/forgot-password

Request Body:
{
  "email": "user@example.com"
}

Response: 200 OK
{
  "message": "Password reset link sent to email"
}
```

### 6. Reset Password

```http
POST /auth/reset-password

Request Body:
{
  "token": "reset_token_from_email",
  "newPassword": "NewSecurePass123!"
}

Response: 200 OK
{
  "message": "Password reset successfully"
}
```

---

## User Profile Endpoints

### 1. Get User Profile

```http
GET /users/profile
Authorization: Bearer <token>

Response: 200 OK
{
  "userId": 1,
  "email": "john@example.com",
  "fullName": "John Doe",
  "phone": "+1-234-567-8900",
  "role": "JOB_SEEKER",
  "status": "ACTIVE",
  "location": "San Francisco, CA",
  "bio": "Full-stack developer with 5 years experience",
  "yearsOfExperience": 5,
  "highestEducation": "BACHELOR",
  "preferredJobTitle": "Senior Developer",
  "profilePictureUrl": "https://cdn.example.com/profile.jpg",
  "cvUrl": "https://cdn.example.com/cv.pdf",
  "createdAt": "2023-01-15T10:30:00Z",
  "updatedAt": "2024-01-20T15:45:00Z"
}
```

### 2. Update User Profile

```http
PUT /users/profile
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "fullName": "John Doe",
  "phone": "+1-234-567-8900",
  "location": "San Francisco, CA",
  "bio": "Full-stack developer with 5+ years experience",
  "yearsOfExperience": 5,
  "highestEducation": "BACHELOR",
  "preferredJobTitle": "Senior Developer"
}

Response: 200 OK
{
  "userId": 1,
  "email": "john@example.com",
  "fullName": "John Doe",
  ... // updated profile
}
```

### 3. Upload CV

```http
POST /users/profile/cv
Authorization: Bearer <token>
Content-Type: multipart/form-data

Form Data:
- file: <PDF or DOC file>

Response: 200 OK
{
  "message": "CV uploaded successfully",
  "cvUrl": "https://cdn.example.com/cv_12345.pdf",
  "parsedSkills": ["Java", "Spring Boot", "MySQL", "REST API"]
}
```

### 4. Get User Skills

```http
GET /users/skills
Authorization: Bearer <token>

Response: 200 OK
{
  "skills": [
    {
      "userSkillId": 1,
      "skillId": 5,
      "name": "Java",
      "category": "Programming",
      "proficiencyLevel": "EXPERT",
      "yearsOfExperience": 5,
      "endorsedCount": 12
    },
    {
      "userSkillId": 2,
      "skillId": 10,
      "name": "Spring Boot",
      "category": "Framework",
      "proficiencyLevel": "ADVANCED",
      "yearsOfExperience": 3,
      "endorsedCount": 8
    }
  ]
}
```

### 5. Add/Update User Skill

```http
POST /users/skills
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "skillId": 5,
  "proficiencyLevel": "EXPERT",
  "yearsOfExperience": 5
}

Response: 201 Created
{
  "userSkillId": 1,
  "skillId": 5,
  "name": "Java",
  "proficiencyLevel": "EXPERT",
  "yearsOfExperience": 5
}
```

### 6. Delete User Skill

```http
DELETE /users/skills/{userSkillId}
Authorization: Bearer <token>

Response: 204 No Content
```

---

## Job Endpoints

### 1. Create Job Post (Company)

```http
POST /jobs
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "title": "Senior Backend Engineer",
  "description": "We are looking for an experienced backend engineer...",
  "jobType": "FULL_TIME",
  "category": "Software Development",
  "salaryMin": 120000,
  "salaryMax": 160000,
  "salaryCurrency": "USD",
  "location": "San Francisco, CA",
  "isRemote": true,
  "officeAddress": "123 Tech Street",
  "experienceLevel": "SENIOR",
  "yearsRequired": 5,
  "closingDate": "2024-02-28T23:59:59Z",
  "requiredSkills": [
    {
      "skillId": 5,
      "proficiencyLevel": "ADVANCED",
      "isMandatory": true
    },
    {
      "skillId": 10,
      "proficiencyLevel": "INTERMEDIATE",
      "isMandatory": false
    }
  ]
}

Response: 201 Created
{
  "jobId": 101,
  "companyId": 50,
  "title": "Senior Backend Engineer",
  "status": "DRAFT",
  "postedDate": "2024-01-20T10:30:00Z"
}
```

### 2. Get All Jobs (with filters)

```http
GET /jobs?page=1&limit=20&location=San%20Francisco&salary_min=100000&salary_max=200000&job_type=FULL_TIME&remote=true&skills=5,10&company_id=50&status=PUBLISHED
Authorization: Bearer <token>

Query Parameters:
- page: int (default: 1)
- limit: int (default: 20, max: 100)
- location: string (case-insensitive, partial match)
- salary_min: decimal
- salary_max: decimal
- job_type: FULL_TIME|PART_TIME|CONTRACT|INTERNSHIP|FREELANCE
- remote: boolean
- skills: comma-separated skill IDs
- company_id: long
- status: PUBLISHED|DRAFT|CLOSED|ARCHIVED
- sort: title|salary|posted_date (default: posted_date desc)

Response: 200 OK
{
  "data": [
    {
      "jobId": 101,
      "companyId": 50,
      "companyName": "Tech Corp",
      "companyLogo": "https://cdn.example.com/logo.png",
      "title": "Senior Backend Engineer",
      "description": "...",
      "jobType": "FULL_TIME",
      "location": "San Francisco, CA",
      "isRemote": true,
      "salaryMin": 120000,
      "salaryMax": 160000,
      "experienceLevel": "SENIOR",
      "yearsRequired": 5,
      "status": "PUBLISHED",
      "postedDate": "2024-01-20T10:30:00Z",
      "viewCount": 245,
      "applicationCount": 12,
      "requiredSkills": [
        {
          "skillId": 5,
          "name": "Java",
          "proficiencyLevel": "ADVANCED",
          "isMandatory": true
        }
      ]
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "totalRecords": 156,
    "totalPages": 8
  }
}
```

### 3. Get Job Details

```http
GET /jobs/{jobId}
Authorization: Bearer <token>

Response: 200 OK
{
  "jobId": 101,
  "companyId": 50,
  "companyName": "Tech Corp",
  "companyDescription": "...",
  "companyWebsite": "https://techcorp.com",
  "title": "Senior Backend Engineer",
  "description": "...",
  "jobType": "FULL_TIME",
  "location": "San Francisco, CA",
  "isRemote": true,
  "salaryMin": 120000,
  "salaryMax": 160000,
  "experienceLevel": "SENIOR",
  "yearsRequired": 5,
  "status": "PUBLISHED",
  "postedDate": "2024-01-20T10:30:00Z",
  "closingDate": "2024-02-28T23:59:59Z",
  "viewCount": 245,
  "applicationCount": 12,
  "requiredSkills": [
    {
      "skillId": 5,
      "name": "Java",
      "proficiencyLevel": "ADVANCED",
      "isMandatory": true
    }
  ]
}
```

### 4. Update Job (Company)

```http
PUT /jobs/{jobId}
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "title": "Senior Backend Engineer - Updated",
  "description": "...",
  // ... other fields
}

Response: 200 OK
{
  "jobId": 101,
  "title": "Senior Backend Engineer - Updated",
  // ... updated data
}
```

### 5. Publish Job (Change status)

```http
PATCH /jobs/{jobId}/publish
Authorization: Bearer <token>

Response: 200 OK
{
  "jobId": 101,
  "status": "PUBLISHED",
  "message": "Job published successfully"
}
```

### 6. Close Job

```http
PATCH /jobs/{jobId}/close
Authorization: Bearer <token>

Response: 200 OK
{
  "jobId": 101,
  "status": "CLOSED",
  "message": "Job closed successfully"
}
```

### 7. Delete Job (Draft only)

```http
DELETE /jobs/{jobId}
Authorization: Bearer <token>

Response: 204 No Content
```

---

## Application Endpoints

### 1. Apply for Job

```http
POST /applications/apply
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "jobId": 101,
  "coverLetter": "I am interested in this position because..."
}

Response: 201 Created
{
  "applicationId": 501,
  "jobId": 101,
  "userId": 1,
  "status": "APPLIED",
  "matchScore": 85.5,
  "appliedDate": "2024-01-21T14:22:00Z"
}

Error: 409 Conflict (already applied)
{
  "error": "You have already applied for this job",
  "code": "DUPLICATE_APPLICATION"
}
```

### 2. Get User's Applications

```http
GET /applications?page=1&limit=20&status=APPLIED&sort=applied_date
Authorization: Bearer <token>

Query Parameters:
- page: int
- limit: int
- status: APPLIED|UNDER_REVIEW|SHORTLISTED|INTERVIEWED|OFFERED|REJECTED|ACCEPTED|WITHDRAWN
- sort: applied_date|match_score|status (with asc/desc)

Response: 200 OK
{
  "data": [
    {
      "applicationId": 501,
      "jobId": 101,
      "jobTitle": "Senior Backend Engineer",
      "companyName": "Tech Corp",
      "companyLogo": "...",
      "status": "APPLIED",
      "matchScore": 85.5,
      "appliedDate": "2024-01-21T14:22:00Z",
      "reviewedDate": null,
      "rejectionReason": null
    }
  ],
  "pagination": { ... }
}
```

### 3. Get Application Details

```http
GET /applications/{applicationId}
Authorization: Bearer <token>

Response: 200 OK
{
  "applicationId": 501,
  "jobId": 101,
  "userId": 1,
  "jobTitle": "Senior Backend Engineer",
  "companyName": "Tech Corp",
  "status": "APPLIED",
  "matchScore": 85.5,
  "skillMatch": 90,
  "experienceMatch": 80,
  "locationMatch": 75,
  "coverLetter": "I am interested...",
  "appliedDate": "2024-01-21T14:22:00Z",
  "history": [
    {
      "status": "APPLIED",
      "changedAt": "2024-01-21T14:22:00Z"
    }
  ]
}
```

### 4. Withdraw Application

```http
PATCH /applications/{applicationId}/withdraw
Authorization: Bearer <token>

Response: 200 OK
{
  "applicationId": 501,
  "status": "WITHDRAWN",
  "message": "Application withdrawn successfully"
}
```

### 5. Get Job Applicants (Company)

```http
GET /jobs/{jobId}/applicants?page=1&limit=20&status=SHORTLISTED
Authorization: Bearer <token>

Response: 200 OK
{
  "data": [
    {
      "applicationId": 501,
      "userId": 1,
      "userName": "John Doe",
      "userEmail": "john@example.com",
      "userPhone": "+1-234-567-8900",
      "profilePicture": "...",
      "status": "SHORTLISTED",
      "matchScore": 85.5,
      "yearsOfExperience": 5,
      "appliedDate": "2024-01-21T14:22:00Z",
      "cvUrl": "..."
    }
  ],
  "pagination": { ... }
}
```

### 6. Update Application Status (Company)

```http
PATCH /applications/{applicationId}/status
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "status": "SHORTLISTED",
  "note": "Selected for next round"
}

Response: 200 OK
{
  "applicationId": 501,
  "status": "SHORTLISTED",
  "updatedAt": "2024-01-22T10:15:00Z"
}
```

### 7. Reject Application (Company)

```http
PATCH /applications/{applicationId}/reject
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "reason": "Not matching required experience"
}

Response: 200 OK
{
  "applicationId": 501,
  "status": "REJECTED",
  "rejectionReason": "Not matching required experience"
}
```

---

## Matching & Recommendation Endpoints

### 1. Get Recommended Jobs (for Job Seeker)

```http
GET /recommendations/jobs?page=1&limit=20
Authorization: Bearer <token>

Response: 200 OK
{
  "data": [
    {
      "jobId": 101,
      "title": "Senior Backend Engineer",
      "companyName": "Tech Corp",
      "matchScore": 87.5,
      "skillMatch": 90,
      "experienceMatch": 85,
      "locationMatch": 80,
      "salaryMatch": 92,
      "reason": "85% of required skills match your expertise"
    }
  ],
  "pagination": { ... }
}
```

### 2. Get Recommended Candidates (for Company)

```http
GET /recommendations/candidates?page=1&limit=20&jobId=101
Authorization: Bearer <token>

Response: 200 OK
{
  "data": [
    {
      "userId": 1,
      "userName": "John Doe",
      "userEmail": "john@example.com",
      "profilePicture": "...",
      "matchScore": 88.5,
      "skillMatch": 92,
      "experienceMatch": 85,
      "locationMatch": 80,
      "yearsOfExperience": 5,
      "reason": "Strong technical skills and relevant experience"
    }
  ],
  "pagination": { ... }
}
```

### 3. Get Skill Gap Analysis (for Job Seeker)

```http
GET /recommendations/skill-gap?jobId=101
Authorization: Bearer <token>

Response: 200 OK
{
  "jobId": 101,
  "jobTitle": "Senior Backend Engineer",
  "skillsYouHave": [
    {
      "skillId": 5,
      "name": "Java",
      "yourLevel": "EXPERT",
      "requiredLevel": "ADVANCED",
      "status": "EXCEEDED"
    }
  ],
  "skillsYouNeed": [
    {
      "skillId": 20,
      "name": "Kubernetes",
      "yourLevel": null,
      "requiredLevel": "INTERMEDIATE",
      "status": "MISSING",
      "learningResources": ["URL1", "URL2"]
    }
  ]
}
```

---

## Company Endpoints

### 1. Create Company (COMPANY_ADMIN role)

```http
POST /companies
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "name": "Tech Corp",
  "description": "A leading tech company...",
  "website": "https://techcorp.com",
  "industry": "Software Development",
  "companySize": "LARGE",
  "foundedYear": 2010,
  "headquartersLocation": "San Francisco, CA",
  "contactEmail": "hr@techcorp.com",
  "contactPhone": "+1-415-555-0100"
}

Response: 201 Created
{
  "companyId": 50,
  "name": "Tech Corp",
  "status": "PENDING",
  "createdAt": "2024-01-20T10:30:00Z"
}
```

### 2. Get Company Profile

```http
GET /companies/{companyId}
Authorization: Bearer <token>

Response: 200 OK
{
  "companyId": 50,
  "name": "Tech Corp",
  "description": "...",
  "website": "...",
  "logo": "...",
  "coverImage": "...",
  "industry": "Software Development",
  "companySize": "LARGE",
  "foundedYear": 2010,
  "status": "VERIFIED",
  "verifiedAt": "2024-01-25T08:00:00Z",
  "activeJobCount": 5,
  "totalApplications": 150,
  "createdAt": "2024-01-20T10:30:00Z"
}
```

### 3. Update Company Profile

```http
PUT /companies/{companyId}
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "description": "...",
  "website": "...",
  // ... other fields
}

Response: 200 OK
{
  "companyId": 50,
  "name": "Tech Corp",
  // ... updated data
}
```

### 4. Upload Company Logo

```http
POST /companies/{companyId}/logo
Authorization: Bearer <token>
Content-Type: multipart/form-data

Form Data:
- file: <PNG or JPG file>

Response: 200 OK
{
  "logoUrl": "https://cdn.example.com/logo_123.png"
}
```

### 5. Get Company Jobs

```http
GET /companies/{companyId}/jobs?page=1&status=PUBLISHED
Authorization: Bearer <token>

Response: 200 OK
{
  "data": [
    {
      "jobId": 101,
      "title": "Senior Backend Engineer",
      "status": "PUBLISHED",
      "applicationCount": 12,
      "viewCount": 245
    }
  ],
  "pagination": { ... }
}
```

### 6. Get Company Analytics

```http
GET /companies/{companyId}/analytics?period=7days
Authorization: Bearer <token>

Query Parameters:
- period: 7days|30days|90days|12months

Response: 200 OK
{
  "period": "7days",
  "totalJobsPosted": 3,
  "totalApplications": 45,
  "avgTimeToHire": 18,
  "jobsByStatus": {
    "PUBLISHED": 5,
    "CLOSED": 2,
    "DRAFT": 1
  },
  "topAppliedJobs": [
    {
      "jobId": 101,
      "title": "Senior Backend Engineer",
      "applicationCount": 18
    }
  ]
}
```

---

## Contact Request Endpoints

### 1. Send Contact Request

```http
POST /contact-requests
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "receiverId": 5,
  "jobId": 101,
  "message": "Hi, I'm interested in discussing this role further..."
}

Response: 201 Created
{
  "contactRequestId": 1001,
  "senderId": 1,
  "receiverId": 5,
  "status": "PENDING",
  "createdAt": "2024-01-21T15:30:00Z"
}
```

### 2. Get Contact Requests

```http
GET /contact-requests?page=1&status=PENDING
Authorization: Bearer <token>

Response: 200 OK
{
  "data": [
    {
      "contactRequestId": 1001,
      "senderId": 1,
      "senderName": "John Doe",
      "senderEmail": "john@example.com",
      "message": "Hi, I'm interested...",
      "status": "PENDING",
      "createdAt": "2024-01-21T15:30:00Z"
    }
  ],
  "pagination": { ... }
}
```

### 3. Accept Contact Request

```http
PATCH /contact-requests/{contactRequestId}/accept
Authorization: Bearer <token>

Response: 200 OK
{
  "contactRequestId": 1001,
  "status": "ACCEPTED",
  "message": "Request accepted, you can now message this user"
}
```

### 4. Reject Contact Request

```http
PATCH /contact-requests/{contactRequestId}/reject
Authorization: Bearer <token>

Response: 200 OK
{
  "contactRequestId": 1001,
  "status": "REJECTED"
}
```

---

## Notification Endpoints

### 1. Get Notifications

```http
GET /notifications?page=1&unread=true
Authorization: Bearer <token>

Response: 200 OK
{
  "data": [
    {
      "notificationId": 2001,
      "type": "APPLICATION_RECEIVED",
      "title": "New Application",
      "message": "John Doe applied for Senior Backend Engineer",
      "relatedEntityType": "APPLICATION",
      "relatedEntityId": 501,
      "isRead": false,
      "createdAt": "2024-01-21T15:30:00Z"
    }
  ],
  "unreadCount": 5,
  "pagination": { ... }
}
```

### 2. Mark Notification as Read

```http
PATCH /notifications/{notificationId}/read
Authorization: Bearer <token>

Response: 200 OK
{
  "notificationId": 2001,
  "isRead": true
}
```

### 3. Mark All Notifications as Read

```http
PATCH /notifications/mark-all-read
Authorization: Bearer <token>

Response: 200 OK
{
  "message": "All notifications marked as read"
}
```

---

## Admin Endpoints

### 1. Get All Users (Admin)

```http
GET /admin/users?page=1&role=JOB_SEEKER&status=ACTIVE
Authorization: Bearer <token>

Response: 200 OK
{
  "data": [
    {
      "userId": 1,
      "email": "user@example.com",
      "fullName": "John Doe",
      "role": "JOB_SEEKER",
      "status": "ACTIVE",
      "createdAt": "2023-01-15T10:30:00Z",
      "lastLoginAt": "2024-01-20T10:30:00Z"
    }
  ],
  "pagination": { ... }
}
```

### 2. Block User (Admin)

```http
PATCH /admin/users/{userId}/block
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "reason": "Spamming users"
}

Response: 200 OK
{
  "userId": 1,
  "status": "BLOCKED",
  "blockedAt": "2024-01-21T16:00:00Z"
}
```

### 3. Unblock User (Admin)

```http
PATCH /admin/users/{userId}/unblock
Authorization: Bearer <token>

Response: 200 OK
{
  "userId": 1,
  "status": "ACTIVE"
}
```

### 4. Get All Companies (Admin)

```http
GET /admin/companies?page=1&status=PENDING
Authorization: Bearer <token>

Response: 200 OK
{
  "data": [
    {
      "companyId": 50,
      "name": "Tech Corp",
      "status": "PENDING",
      "submittedAt": "2024-01-20T10:30:00Z"
    }
  ],
  "pagination": { ... }
}
```

### 5. Verify Company (Admin)

```http
PATCH /admin/companies/{companyId}/verify
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "verificationNote": "Documents verified"
}

Response: 200 OK
{
  "companyId": 50,
  "status": "VERIFIED",
  "verifiedAt": "2024-01-21T16:00:00Z"
}
```

### 6. Reject Company (Admin)

```http
PATCH /admin/companies/{companyId}/reject
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "reason": "Invalid business documents"
}

Response: 200 OK
{
  "companyId": 50,
  "status": "REJECTED",
  "rejectionReason": "Invalid business documents"
}
```

### 7. Get Platform Analytics (Admin)

```http
GET /admin/analytics?period=7days
Authorization: Bearer <token>

Response: 200 OK
{
  "period": "7days",
  "totalUsers": 5234,
  "activeUsers": 1456,
  "totalCompanies": 234,
  "verifiedCompanies": 198,
  "totalJobs": 1203,
  "activeJobs": 856,
  "totalApplications": 12540,
  "userGrowth": 123,
  "applicationTrend": [
    {
      "date": "2024-01-15",
      "applicationCount": 1234
    }
  ],
  "topSkills": [
    {
      "skillId": 5,
      "name": "Java",
      "demandCount": 523
    }
  ],
  "topLocations": [
    {
      "location": "San Francisco, CA",
      "jobCount": 345
    }
  ]
}
```

### 8. Get Admin Activity Logs

```http
GET /admin/logs?page=1&actionType=USER_BLOCKED
Authorization: Bearer <token>

Response: 200 OK
{
  "data": [
    {
      "logId": 3001,
      "adminId": 2,
      "adminName": "Admin User",
      "actionType": "USER_BLOCKED",
      "targetEntityType": "USER",
      "targetEntityId": 1,
      "details": {
        "reason": "Spamming users"
      },
      "timestamp": "2024-01-21T16:00:00Z"
    }
  ],
  "pagination": { ... }
}
```

---

## Skill Endpoints

### 1. Get All Skills

```http
GET /skills?page=1&category=Programming
Authorization: Bearer <token>

Response: 200 OK
{
  "data": [
    {
      "skillId": 5,
      "name": "Java",
      "category": "Programming",
      "description": "...",
      "demandCount": 523,
      "popularityScore": 95
    }
  ],
  "pagination": { ... }
}
```

### 2. Search Skills (Auto-complete)

```http
GET /skills/search?q=java
Authorization: Bearer <token>

Response: 200 OK
{
  "suggestions": [
    {
      "skillId": 5,
      "name": "Java",
      "category": "Programming"
    },
    {
      "skillId": 6,
      "name": "JavaScript",
      "category": "Programming"
    }
  ]
}
```

---

## Error Responses

### Standard Error Format

```json
{
  "error": "Descriptive error message",
  "code": "ERROR_CODE",
  "timestamp": "2024-01-21T16:00:00Z",
  "details": {
    "field": "fieldName",
    "message": "Validation error message"
  }
}
```

### Common HTTP Status Codes

| Code | Meaning                                              |
| ---- | ---------------------------------------------------- |
| 200  | OK - Request successful                              |
| 201  | Created - Resource created                           |
| 204  | No Content - Successful but no content to return     |
| 400  | Bad Request - Invalid input                          |
| 401  | Unauthorized - Invalid/missing auth token            |
| 403  | Forbidden - User lacks permission                    |
| 404  | Not Found - Resource doesn't exist                   |
| 409  | Conflict - Resource already exists or state conflict |
| 429  | Too Many Requests - Rate limit exceeded              |
| 500  | Internal Server Error                                |
