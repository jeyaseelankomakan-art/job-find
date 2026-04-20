package com.jobmatch.api.model.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating/updating company
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRequest {
    
    @NotBlank(message = "Company name is required")
    private String name;
    
    private String description;
    
    private String website;
    
    private String industry;
    
    private String companySize;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    private String logoUrl;
}
