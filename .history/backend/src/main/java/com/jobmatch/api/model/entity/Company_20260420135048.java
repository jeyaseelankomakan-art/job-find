package com.jobmatch.api.model.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Company Entity
 * Represents companies/employers on the platform
 */
@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_company_name", columnList = "name"),
        @Index(name = "idx_company_verified", columnList = "verified"),
        @Index(name = "idx_company_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 255)
    private String website;
    
    @Column(length = 100)
    private String industry;
    
    @Column(length = 50)
    private String companySize; // startup, small, medium, large, enterprise
    
    @Column(length = 255)
    private String location;
    
    @Column(length = 255)
    private String logoUrl;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;
    
    @Column(name = "verified_by")
    private Long verifiedBy; // Admin ID
    
    @Column(name = "verification_date")
    private Date verificationDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Date createdAt = new Date();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Date updatedAt = new Date();
    
    // Relationships
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private Set<Job> jobs;
    
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private Set<CompanyAdmin> admins;
    
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

