package com.jobmatch.api.model.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.Date;

/**
 * AdminLog Entity
 * Audit trail for admin actions
 */
@Entity
@Table(name = "admin_logs", indexes = {
        @Index(name = "idx_admin_id", columnList = "admin_id"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "admin_id", nullable = false)
    private Long adminId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminAction action; // USER_BLOCKED, USER_UNBLOCKED, COMPANY_VERIFIED, COMPANY_REJECTED, JOB_MODERATED, CONTENT_REMOVED
    
    @Column(length = 255)
    private String targetType; // User, Company, Job, etc.
    
    @Column(name = "target_id")
    private Long targetId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Date createdAt = new Date();
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
    
    /**
     * Admin Action Enum
     */
    public enum AdminAction {
        USER_BLOCKED,
        USER_UNBLOCKED,
        COMPANY_VERIFIED,
        COMPANY_REJECTED,
        JOB_MODERATED,
        CONTENT_REMOVED
    }
}

