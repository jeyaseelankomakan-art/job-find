package com.jobmatch.api.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Skill Entity
 * Represents skills that users and jobs can have
 */
@Entity
@Table(name = "skills", indexes = {
        @Index(name = "idx_skill_name", columnList = "name", unique = true),
        @Index(name = "idx_skill_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, length = 50)
    private String category; // Programming, Design, Business, etc.
    
    @Column(nullable = false)
    @Builder.Default
    private Integer popularity = 0; // For sorting/recommendations
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Date createdAt = new Date();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Date updatedAt = new Date();
    
    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL)
    private Set<UserSkill> userSkills;
    
    @JsonIgnore
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL)
    private Set<JobSkill> jobSkills;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}

