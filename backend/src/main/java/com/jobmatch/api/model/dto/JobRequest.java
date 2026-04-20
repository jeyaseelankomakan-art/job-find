package com.jobmatch.api.model.dto;

import lombok.*;

import jakarta.validation.constraints.*;

/**
 * Request DTO for creating/updating job
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest {
    
    @NotBlank(message = "Job title is required")
    private String title;
    
    @NotBlank(message = "Job description is required")
    private String description;
    
    private String responsibilities;
    
    private String requirements;
    
    @NotBlank(message = "Job type is required")
    private String jobType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, REMOTE
    
    private String location;
    
    @NotNull(message = "Remote flag is required")
    private Boolean remote;
    
    @NotNull(message = "Minimum salary is required")
    @PositiveOrZero(message = "Minimum salary must be >= 0")
    private Long salaryMin;
    
    @NotNull(message = "Maximum salary is required")
    @PositiveOrZero(message = "Maximum salary must be >= 0")
    private Long salaryMax;
    
    private String currency = "USD";
    
    @NotBlank(message = "Experience level is required")
    private String experienceLevel; // ENTRY, MID, SENIOR, LEAD
}
