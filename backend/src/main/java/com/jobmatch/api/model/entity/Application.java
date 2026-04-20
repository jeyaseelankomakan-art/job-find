package com.jobmatch.api.model.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Application Entity
 * Represents job applications submitted by users
 */
@Entity
@Table(name = "applications", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_job_id", columnList = "job_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_user_job", columnList = "user_id,job_id", unique = true),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "job_id", nullable = false)
    private Long jobId;
    
    @Column(columnDefinition = "TEXT")
    private String coverLetter;
    
    @Column(name = "match_score", nullable = false)
    @Builder.Default
    private Double matchScore = 0.0; // 0-100
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING; // PENDING, REJECTED, ACCEPTED, WITHDRAWN
    
    @Column(name = "reviewed_at")
    private Date reviewedAt;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy; // Company Admin ID
    
    @Column(name = "rejected_reason")
    private String rejectedReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Date createdAt = new Date();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Date updatedAt = new Date();
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", insertable = false, updatable = false)
    private Job job;
    
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
     * Application Status Enum
     */
    public enum ApplicationStatus {
        PENDING,
        REJECTED,
        ACCEPTED,
        WITHDRAWN
    }
}

