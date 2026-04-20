package com.jobmatch.api.model.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.Date;

/**
 * UserSkill Entity
 * Junction table representing skills that a user has with proficiency level
 */
@Entity
@Table(name = "user_skills", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_skill_id", columnList = "skill_id"),
        @Index(name = "idx_user_skill", columnList = "user_id,skill_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "skill_id", nullable = false)
    private Long skillId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProficiencyLevel proficiencyLevel; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    
    @Column(nullable = false)
    @Builder.Default
    private Integer yearsOfExperience = 0;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
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
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private Skill skill;
    
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
     * Proficiency Level Enum
     */
    public enum ProficiencyLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }
}

