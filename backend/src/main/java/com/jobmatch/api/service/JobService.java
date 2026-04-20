package com.jobmatch.api.service;

import com.jobmatch.api.exception.ResourceNotFoundException;
import com.jobmatch.api.model.dto.JobRequest;
import com.jobmatch.api.model.dto.JobSearchCriteria;
import com.jobmatch.api.model.entity.Job;
import com.jobmatch.api.model.entity.JobSkill;
import com.jobmatch.api.repository.JobRepository;
import com.jobmatch.api.repository.JobSkillRepository;
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
 * Job Service
 * Business logic for job operations
 */
@Service
@Transactional
@Slf4j
public class JobService {
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private JobSkillRepository jobSkillRepository;
    
    /**
     * Get job by ID
     */
    public Job getJobById(Long jobId) {
        log.debug("Getting job by ID: {}", jobId);
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
    }
    
    /**
     * Get all published jobs with pagination
     */
    public Page<Job> getAllPublishedJobs(int page, int pageSize) {
        log.debug("Getting all published jobs, page: {}, size: {}", page, pageSize);
        Pageable pageable = PageRequest.of(page, pageSize);
        return jobRepository.findPublished(pageable);
    }
    
    /**
     * Get jobs by company
     */
    public Page<Job> getJobsByCompany(Long companyId, int page, int pageSize) {
        log.debug("Getting jobs for company: {}", companyId);
        Pageable pageable = PageRequest.of(page, pageSize);
        return jobRepository.findByCompanyId(companyId, pageable);
    }
    
    /**
     * Search jobs with advanced filtering
     */
    public Page<Job> searchJobs(JobSearchCriteria criteria) {
        log.debug("Searching jobs with criteria: {}", criteria);
        
        Pageable pageable = PageRequest.of(
                criteria.getPage() != null ? criteria.getPage() : 0,
                criteria.getPageSize() != null ? criteria.getPageSize() : 20
        );
        
        String title = criteria.getTitle();
        String location = criteria.getLocation();
        Long salaryMin = criteria.getSalaryMin();
        Long salaryMax = criteria.getSalaryMax();
        String experienceLevel = criteria.getExperienceLevel();
        String jobType = criteria.getJobType();
        Long companyId = criteria.getCompanyId();
        
        return jobRepository.searchJobs(title, location, salaryMin, salaryMax, 
                experienceLevel, jobType, companyId, pageable);
    }
    
    /**
     * Search jobs by title
     */
    public Page<Job> searchByTitle(String query, int page, int pageSize) {
        log.debug("Searching jobs by title: {}", query);
        Pageable pageable = PageRequest.of(page, pageSize);
        return jobRepository.searchByTitle(query, pageable);
    }
    
    /**
     * Get jobs by location
     */
    public Page<Job> getJobsByLocation(String location, int page, int pageSize) {
        log.debug("Getting jobs by location: {}", location);
        Pageable pageable = PageRequest.of(page, pageSize);
        return jobRepository.findByLocation(location, pageable);
    }
    
    /**
     * Get jobs by experience level
     */
    public Page<Job> getJobsByExperienceLevel(String level, int page, int pageSize) {
        log.debug("Getting jobs by experience level: {}", level);
        Pageable pageable = PageRequest.of(page, pageSize);
        return jobRepository.findByExperienceLevel(level, pageable);
    }
    
    /**
     * Get jobs by job type
     */
    public Page<Job> getJobsByJobType(String jobType, int page, int pageSize) {
        log.debug("Getting jobs by job type: {}", jobType);
        Pageable pageable = PageRequest.of(page, pageSize);
        return jobRepository.findByJobType(jobType, pageable);
    }
    
    /**
     * Create new job (draft)
     */
    public Job createJob(Long companyId, JobRequest request) {
        log.info("Creating new job for company: {}", companyId);
        
        Job job = Job.builder()
                .companyId(companyId)
                .title(request.getTitle())
                .description(request.getDescription())
                .responsibilities(request.getResponsibilities())
                .requirements(request.getRequirements())
                .jobType(request.getJobType())
                .location(request.getLocation())
                .remote(request.getRemote())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .experienceLevel(request.getExperienceLevel())
                .status(Job.JobStatus.DRAFT)
                .build();
        
        job = jobRepository.save(job);
        log.info("Job created with ID: {}", job.getId());
        return job;
    }
    
    /**
     * Publish job
     */
    public Job publishJob(Long jobId) {
        log.info("Publishing job: {}", jobId);
        
        Job job = getJobById(jobId);
        job.setStatus(Job.JobStatus.PUBLISHED);
        job.setPublishedAt(new Date());
        
        job = jobRepository.save(job);
        log.info("Job published: {}", jobId);
        return job;
    }
    
    /**
     * Update job
     */
    public Job updateJob(Long jobId, JobRequest request) {
        log.info("Updating job: {}", jobId);
        
        Job job = getJobById(jobId);
        
        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getResponsibilities() != null) job.setResponsibilities(request.getResponsibilities());
        if (request.getRequirements() != null) job.setRequirements(request.getRequirements());
        if (request.getJobType() != null) job.setJobType(request.getJobType());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getRemote() != null) job.setRemote(request.getRemote());
        if (request.getSalaryMin() != null) job.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null) job.setSalaryMax(request.getSalaryMax());
        if (request.getCurrency() != null) job.setCurrency(request.getCurrency());
        if (request.getExperienceLevel() != null) job.setExperienceLevel(request.getExperienceLevel());
        
        job = jobRepository.save(job);
        log.info("Job updated: {}", jobId);
        return job;
    }
    
    /**
     * Close job
     */
    public Job closeJob(Long jobId) {
        log.info("Closing job: {}", jobId);
        
        Job job = getJobById(jobId);
        job.setStatus(Job.JobStatus.CLOSED);
        job.setClosedAt(new Date());
        
        job = jobRepository.save(job);
        log.info("Job closed: {}", jobId);
        return job;
    }
    
    /**
     * Delete job
     */
    public void deleteJob(Long jobId) {
        log.info("Deleting job: {}", jobId);
        Job job = getJobById(jobId);
        jobRepository.delete(job);
        log.info("Job deleted: {}", jobId);
    }
    
    /**
     * Get job skills
     */
    public List<JobSkill> getJobSkills(Long jobId) {
        log.debug("Getting skills for job: {}", jobId);
        return jobSkillRepository.findByJobId(jobId);
    }
    
    /**
     * Get total published jobs count
     */
    public Long getTotalPublishedJobsCount() {
        log.debug("Getting total published jobs count");
        return jobRepository.countPublished();
    }
}
