package com.jobmatch.api.model.dto;

import lombok.*;

/**
 * DTO for job search criteria
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSearchCriteria {
    
    private String title;
    
    private String location;
    
    private Long salaryMin;
    
    private Long salaryMax;
    
    private String experienceLevel;
    
    private String jobType;
    
    private Boolean remote;
    
    private Long companyId;
    
    private Integer page = 0;
    
    private Integer pageSize = 20;
}
