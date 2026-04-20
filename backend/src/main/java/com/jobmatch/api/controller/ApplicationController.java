package com.jobmatch.api.controller;

import com.jobmatch.api.model.dto.ApiResponse;
import com.jobmatch.api.model.dto.ApplicationRequest;
import com.jobmatch.api.model.entity.Application;
import com.jobmatch.api.service.ApplicationService;
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
 * Application REST Controller
 * Handles job application endpoints
 */
@RestController
@RequestMapping("/api/v1/applications")
@Slf4j
public class ApplicationController {
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private JobService jobService;
    
    @Autowired
    private CompanyService companyService;
    
    /**
     * GET /api/v1/applications/{id}
     * Get application by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Application>> getApplication(@PathVariable Long id) {
        log.info("GET /applications/{}", id);
        Application application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Application found", application));
    }
    
    /**
     * GET /api/v1/applications/user/my
     * Get all applications for current user
     */
    @GetMapping("/user/my")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApiResponse<Page<Application>>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication) {
        log.info("GET /applications/user/my");
        
        Long userId = Long.parseLong(authentication.getName());
        Page<Application> applications = applicationService.getUserApplications(userId, page, pageSize);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Applications retrieved", applications));
    }
    
    /**
     * GET /api/v1/applications/job/{jobId}
     * Get all applications for job (company admin only)
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Page<Application>>> getJobApplications(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication) {
        log.info("GET /applications/job/{}", jobId);
        
        // Verify user is admin of company that owns this job
        Long userId = Long.parseLong(authentication.getName());
        com.jobmatch.api.model.entity.Job job = jobService.getJobById(jobId);
        Boolean isAdmin = companyService.isUserAdminOfCompany(userId, job.getCompanyId());
        
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to view these applications", null));
        }
        
        Page<Application> applications = applicationService.getJobApplications(jobId, page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Applications retrieved", applications));
    }
    
    /**
     * POST /api/v1/applications
     * Submit job application
     */
    @PostMapping
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApiResponse<Application>> submitApplication(
            @Valid @RequestBody ApplicationRequest request,
            Authentication authentication) {
        log.info("POST /applications for job: {}", request.getJobId());
        
        Long userId = Long.parseLong(authentication.getName());
        Application application = applicationService.submitApplication(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Application submitted successfully", application));
    }
    
    /**
     * POST /api/v1/applications/{id}/accept
     * Accept application (company admin only)
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Application>> acceptApplication(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("POST /applications/{}/accept", id);
        
        // Verify user is admin of company
        Long userId = Long.parseLong(authentication.getName());
        Application application = applicationService.getApplicationById(id);
        com.jobmatch.api.model.entity.Job job = jobService.getJobById(application.getJobId());
        Boolean isAdmin = companyService.isUserAdminOfCompany(userId, job.getCompanyId());
        
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to accept this application", null));
        }
        
        Application updatedApplication = applicationService.acceptApplication(id, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Application accepted", updatedApplication));
    }
    
    /**
     * POST /api/v1/applications/{id}/reject
     * Reject application (company admin only)
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Application>> rejectApplication(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("POST /applications/{}/reject", id);
        
        // Verify user is admin of company
        Long userId = Long.parseLong(authentication.getName());
        Application application = applicationService.getApplicationById(id);
        com.jobmatch.api.model.entity.Job job = jobService.getJobById(application.getJobId());
        Boolean isAdmin = companyService.isUserAdminOfCompany(userId, job.getCompanyId());
        
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to reject this application", null));
        }
        
        Application updatedApplication = applicationService.rejectApplication(id, userId, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, "Application rejected", updatedApplication));
    }
    
    /**
     * POST /api/v1/applications/{id}/withdraw
     * Withdraw application (job seeker only)
     */
    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApiResponse<Application>> withdrawApplication(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("POST /applications/{}/withdraw", id);
        
        // Verify user owns this application
        Long userId = Long.parseLong(authentication.getName());
        Application application = applicationService.getApplicationById(id);
        
        if (!application.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to withdraw this application", null));
        }
        
        Application updatedApplication = applicationService.withdrawApplication(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Application withdrawn", updatedApplication));
    }
    
    /**
     * GET /api/v1/applications/recommended
     * Get recommended jobs for user (match score >= 55%)
     */
    @GetMapping("/recommended")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApiResponse<java.util.List<Application>>> getRecommendedJobs(
            Authentication authentication) {
        log.info("GET /applications/recommended");
        
        Long userId = Long.parseLong(authentication.getName());
        java.util.List<Application> recommendations = applicationService.getRecommendedJobs(userId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Recommended jobs retrieved", recommendations));
    }
}
