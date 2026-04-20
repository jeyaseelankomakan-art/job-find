package com.jobmatch.api.model.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.Date;

/**
 * JobSkill Entity
 * Junction table representing skills required for a job
 */
@Entity
@Table(name = "job_skills", indexes = {
        @Index(name = "idx_job_id", columnList = "job_id"),
        @Index(name = "idx_job_skill_id", columnList = "skill_id"),
        @Index(name = "idx_job_skill", columnList = "job_id,skill_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_id", nullable = false)
    private Long jobId;
    
    @Column(name = "skill_id", nullable = false)
    private Long skillId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserSkill.ProficiencyLevel requiredLevel; // Minimum required proficiency
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean mandatory = true; // Whether this skill is mandatory
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Date createdAt = new Date();
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", insertable = false, updatable = false)
    private Job job;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private Skill skill;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}

