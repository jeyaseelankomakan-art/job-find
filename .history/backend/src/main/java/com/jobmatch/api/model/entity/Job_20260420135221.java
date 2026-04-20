package com.jobmatch.api.model.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Job Entity
 * Represents job postings
 */
@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_company_id", columnList = "company_id"),
        @Index(name = "idx_job_status", columnList = "status"),
        @Index(name = "idx_job_location", columnList = "location"),
        @Index(name = "idx_job_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String responsibilities;
    
    @Column(columnDefinition = "TEXT")
    private String requirements;
    
    @Column(length = 100)
    private String jobType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, REMOTE
    
    @Column(length = 255)
    private String location;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean remote = false;
    
    @Column(nullable = false)
    private Long salaryMin;
    
    @Column(nullable = false)
    private Long salaryMax;
    
    @Column(length = 50)
    private String currency = "USD";
    
    @Column(length = 50)
    private String experienceLevel; // ENTRY, MID, SENIOR, LEAD
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JobStatus status = JobStatus.DRAFT; // DRAFT, PUBLISHED, CLOSED, EXPIRED
    
    @Column(name = "published_at")
    private Date publishedAt;
    
    @Column(name = "closed_at")
    private Date closedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Date createdAt = new Date();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Date updatedAt = new Date();
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;
    
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private Set<JobSkill> skills;
    
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private Set<Application> applications;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
    
    /**
     * Job Status Enum
     */
    public enum JobStatus {
        DRAFT,
        PUBLISHED,
        CLOSED,
        EXPIRED
    }
}

