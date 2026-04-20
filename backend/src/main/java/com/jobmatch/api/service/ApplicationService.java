package com.jobmatch.api.service;

import com.jobmatch.api.exception.DuplicateResourceException;
import com.jobmatch.api.exception.ResourceNotFoundException;
import com.jobmatch.api.model.dto.ApplicationRequest;
import com.jobmatch.api.model.entity.Application;
import com.jobmatch.api.repository.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Application Service
 * Business logic for job applications
 */
@Service
@Transactional
@Slf4j
public class ApplicationService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private MatchingService matchingService;
    
    @Autowired
    private JobService jobService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Submit job application
     * Includes duplicate check and match score calculation
     */
    public Application submitApplication(Long userId, ApplicationRequest request) {
        log.info("Submitting application from user: {} for job: {}", userId, request.getJobId());
        
        // Verify user exists
        userService.getUserById(userId);
        
        // Verify job exists
        jobService.getJobById(request.getJobId());
        
        // Check for duplicate application
        Boolean hasApplied = applicationRepository.hasApplied(userId, request.getJobId());
        if (hasApplied) {
            log.warn("User {} already applied for job {}", userId, request.getJobId());
            throw new DuplicateResourceException("You have already applied for this job");
        }
        
        // Calculate match score
        Double matchScore = matchingService.calculateMatchScore(userId, request.getJobId());
        log.info("Calculated match score: {} for user: {}, job: {}", matchScore, userId, request.getJobId());
        
        // Create and save application
        Application application = Application.builder()
                .userId(userId)
                .jobId(request.getJobId())
                .coverLetter(request.getCoverLetter())
                .matchScore(matchScore)
                .status(Application.ApplicationStatus.PENDING)
                .build();
        
        application = applicationRepository.save(application);
        log.info("Application submitted with ID: {}", application.getId());
        
        return application;
    }
    
    /**
     * Get application by ID
     */
    public Application getApplicationById(Long applicationId) {
        log.debug("Getting application by ID: {}", applicationId);
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));
    }
    
    /**
     * Get all applications for user
     */
    public Page<Application> getUserApplications(Long userId, int page, int pageSize) {
        log.debug("Getting applications for user: {}, page: {}, size: {}", userId, page, pageSize);
        Pageable pageable = PageRequest.of(page, pageSize);
        return applicationRepository.findByUserId(userId, pageable);
    }
    
    /**
     * Get all applications for job
     */
    public Page<Application> getJobApplications(Long jobId, int page, int pageSize) {
        log.debug("Getting applications for job: {}, page: {}, size: {}", jobId, page, pageSize);
        Pageable pageable = PageRequest.of(page, pageSize);
        return applicationRepository.findByJobId(jobId, pageable);
    }
    
    /**
     * Accept application
     */
    public Application acceptApplication(Long applicationId, Long reviewedBy) {
        log.info("Accepting application: {}", applicationId);
        
        Application application = getApplicationById(applicationId);
        application.setStatus(Application.ApplicationStatus.ACCEPTED);
        application.setReviewedAt(new Date());
        application.setReviewedBy(reviewedBy);
        
        application = applicationRepository.save(application);
        log.info("Application accepted: {}", applicationId);
        
        return application;
    }
    
    /**
     * Reject application
     */
    public Application rejectApplication(Long applicationId, Long reviewedBy, String reason) {
        log.info("Rejecting application: {}", applicationId);
        
        Application application = getApplicationById(applicationId);
        application.setStatus(Application.ApplicationStatus.REJECTED);
        application.setReviewedAt(new Date());
        application.setReviewedBy(reviewedBy);
        application.setRejectedReason(reason);
        
        application = applicationRepository.save(application);
        log.info("Application rejected: {}", applicationId);
        
        return application;
    }
    
    /**
     * Withdraw application
     */
    public Application withdrawApplication(Long applicationId) {
        log.info("Withdrawing application: {}", applicationId);
        
        Application application = getApplicationById(applicationId);
        
        if (application.getStatus() != Application.ApplicationStatus.PENDING) {
            throw new RuntimeException("Can only withdraw pending applications");
        }
        
        application.setStatus(Application.ApplicationStatus.WITHDRAWN);
        application = applicationRepository.save(application);
        
        log.info("Application withdrawn: {}", applicationId);
        return application;
    }
    
    /**
     * Get applications by status
     */
    public Page<Application> getApplicationsByStatus(Application.ApplicationStatus status, int page, int pageSize) {
        log.debug("Getting applications by status: {}", status);
        Pageable pageable = PageRequest.of(page, pageSize);
        return applicationRepository.findByStatus(status, pageable);
    }
    
    /**
     * Get recommended jobs for user (with match score >= 55%)
     */
    public List<Application> getRecommendedJobs(Long userId) {
        log.debug("Getting recommended jobs for user: {}", userId);
        return applicationRepository.findRecommendedJobsForUser(userId);
    }
    
    /**
     * Count applications by status
     */
    public Long countApplicationsByStatus(Application.ApplicationStatus status) {
        log.debug("Counting applications by status: {}", status);
        return applicationRepository.countByStatus(status);
    }
    
    /**
     * Count applications for job by status
     */
    public Long countJobApplicationsByStatus(Long jobId, Application.ApplicationStatus status) {
        log.debug("Counting applications for job: {} by status: {}", jobId, status);
        return applicationRepository.countByJobIdAndStatus(jobId, status);
    }
}
