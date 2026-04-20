package com.jobmatch.api.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;
    
    @Column(length = 20)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(length = 255)
    private String location;
    
    // Job seeker specific
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "highest_education")
    private EducationLevel highestEducation;
    
    @Column(name = "preferred_job_title", length = 255)
    private String preferredJobTitle;
    
    @Column(name = "cv_url", length = 500)
    private String cvUrl;
    
    @Column(name = "cv_parsed_skills", columnDefinition = "JSON")
    private String cvParsedSkills;
    
    @Column(name = "salary_expectation")
    private Long salaryExpectation;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum UserRole {
        JOB_SEEKER,
        COMPANY_ADMIN,
        SYSTEM_ADMIN
    }
    
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        BLOCKED,
        PENDING_VERIFICATION
    }
    
    public enum EducationLevel {
        HIGH_SCHOOL,
        BACHELOR,
        MASTER,
        PHD,
        OTHER
    }
}
