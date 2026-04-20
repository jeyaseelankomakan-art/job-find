package com.jobmatch.api.model.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Notification Entity
 * Represents user notifications for various events
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_read", columnList = "is_read"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type; // APPLICATION_SUBMITTED, APPLICATION_ACCEPTED, APPLICATION_REJECTED, COMPANY_VERIFIED, USER_BLOCKED, JOB_PUBLISHED, JOB_CLOSED
    
    @Column(length = 255, nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "related_id")
    private Long relatedId; // Application ID, Company ID, Job ID, User ID depending on type
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    @Builder.Default
    
    @Column(name = "read_at")
    private Date readAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt = new Date();
    @Builder.Default
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
    
    /**
     * Notification Type Enum
     */
    public enum NotificationType {
        APPLICATION_SUBMITTED,
        APPLICATION_ACCEPTED,
        APPLICATION_REJECTED,
        COMPANY_VERIFIED,
        USER_BLOCKED,
        JOB_PUBLISHED,
        JOB_CLOSED
    }
}

