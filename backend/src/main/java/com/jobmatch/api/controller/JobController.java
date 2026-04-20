package com.jobmatch.api.controller;

import com.jobmatch.api.model.dto.ApiResponse;
import com.jobmatch.api.model.dto.JobRequest;
import com.jobmatch.api.model.dto.JobSearchCriteria;
import com.jobmatch.api.model.entity.Job;
import com.jobmatch.api.service.CompanyService;
import com.jobmatch.api.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Job REST Controller
 * Handles job-related endpoints
 */
@RestController
@RequestMapping("/api/v1/jobs")
@Slf4j
public class JobController {
    
    @Autowired
    private JobService jobService;
    
    @Autowired
    private CompanyService companyService;
    
    /**
     * GET /api/v1/jobs/{id}
     * Get job by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> getJob(@PathVariable Long id) {
        log.info("GET /jobs/{}", id);
        Job job = jobService.getJobById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Job found", job));
    }
    
    /**
     * GET /api/v1/jobs
     * Get all published jobs (paginated)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Job>>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("GET /jobs, page: {}, pageSize: {}", page, pageSize);
        Page<Job> jobs = jobService.getAllPublishedJobs(page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Jobs retrieved", jobs));
    }
    
    /**
     * POST /api/v1/jobs/search
     * Advanced job search with filtering
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<Job>>> searchJobs(@RequestBody JobSearchCriteria criteria) {
        log.info("POST /jobs/search, criteria: {}", criteria);
        Page<Job> jobs = jobService.searchJobs(criteria);
        return ResponseEntity.ok(new ApiResponse<>(true, "Jobs found", jobs));
    }
    
    /**
     * GET /api/v1/jobs/search/title
     * Search jobs by title
     */
    @GetMapping("/search/title")
    public ResponseEntity<ApiResponse<Page<Job>>> searchByTitle(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("GET /jobs/search/title, query: {}", query);
        Page<Job> jobs = jobService.searchByTitle(query, page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Jobs found", jobs));
    }
    
    /**
     * GET /api/v1/jobs/location/{location}
     * Get jobs by location
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<ApiResponse<Page<Job>>> getJobsByLocation(
            @PathVariable String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("GET /jobs/location/{}", location);
        Page<Job> jobs = jobService.getJobsByLocation(location, page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Jobs found", jobs));
    }
    
    /**
     * GET /api/v1/jobs/experience/{level}
     * Get jobs by experience level
     */
    @GetMapping("/experience/{level}")
    public ResponseEntity<ApiResponse<Page<Job>>> getJobsByExperienceLevel(
            @PathVariable String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("GET /jobs/experience/{}", level);
        Page<Job> jobs = jobService.getJobsByExperienceLevel(level, page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Jobs found", jobs));
    }
    
    /**
     * GET /api/v1/jobs/type/{jobType}
     * Get jobs by job type
     */
    @GetMapping("/type/{jobType}")
    public ResponseEntity<ApiResponse<Page<Job>>> getJobsByJobType(
            @PathVariable String jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("GET /jobs/type/{}", jobType);
        Page<Job> jobs = jobService.getJobsByJobType(jobType, page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Jobs found", jobs));
    }
    
    /**
     * POST /api/v1/companies/{companyId}/jobs
     * Create new job (draft)
     */
    @PostMapping("/company/{companyId}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Job>> createJob(
            @PathVariable Long companyId,
            @Valid @RequestBody JobRequest request,
            Authentication authentication) {
        log.info("POST /jobs/company/{}", companyId);
        
        // Verify user is admin of company
        Long userId = Long.parseLong(authentication.getName());
        Boolean isAdmin = companyService.isUserAdminOfCompany(userId, companyId);
        
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to create jobs for this company", null));
        }
        
        Job job = jobService.createJob(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Job created successfully", job));
    }
    
    /**
     * PUT /api/v1/jobs/{id}
     * Update job
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Job>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request,
            Authentication authentication) {
        log.info("PUT /jobs/{}", id);
        
        Job job = jobService.getJobById(id);
        
        // Verify user is admin of company
        Long userId = Long.parseLong(authentication.getName());
        Boolean isAdmin = companyService.isUserAdminOfCompany(userId, job.getCompanyId());
        
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to update this job", null));
        }
        
        Job updatedJob = jobService.updateJob(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Job updated successfully", updatedJob));
    }
    
    /**
     * POST /api/v1/jobs/{id}/publish
     * Publish job
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Job>> publishJob(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("POST /jobs/{}/publish", id);
        
        Job job = jobService.getJobById(id);
        
        // Verify user is admin of company
        Long userId = Long.parseLong(authentication.getName());
        Boolean isAdmin = companyService.isUserAdminOfCompany(userId, job.getCompanyId());
        
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to publish this job", null));
        }
        
        Job publishedJob = jobService.publishJob(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Job published successfully", publishedJob));
    }
    
    /**
     * POST /api/v1/jobs/{id}/close
     * Close job
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Job>> closeJob(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("POST /jobs/{}/close", id);
        
        Job job = jobService.getJobById(id);
        
        // Verify user is admin of company
        Long userId = Long.parseLong(authentication.getName());
        Boolean isAdmin = companyService.isUserAdminOfCompany(userId, job.getCompanyId());
        
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to close this job", null));
        }
        
        Job closedJob = jobService.closeJob(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Job closed successfully", closedJob));
    }
    
    /**
     * DELETE /api/v1/jobs/{id}
     * Delete job
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("DELETE /jobs/{}", id);
        
        Job job = jobService.getJobById(id);
        
        // Verify user is admin of company
        Long userId = Long.parseLong(authentication.getName());
        Boolean isAdmin = companyService.isUserAdminOfCompany(userId, job.getCompanyId());
        
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to delete this job", null));
        }
        
        jobService.deleteJob(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Job deleted successfully", null));
    }
}
