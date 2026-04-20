package com.jobmatch.api.model.dto;

import lombok.*;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for submitting job application
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequest {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    private String coverLetter;
}
