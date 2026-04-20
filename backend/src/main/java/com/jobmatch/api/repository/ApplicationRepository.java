package com.jobmatch.api.repository;

import com.jobmatch.api.model.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Application Repository
 * Provides database access for Application entity
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    /**
     * Find all applications by user
     */
    Page<Application> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find all applications by job
     */
    Page<Application> findByJobId(Long jobId, Pageable pageable);
    
    /**
     * Find single application by user and job
     */
    @Query("SELECT a FROM Application a WHERE a.userId = :userId AND a.jobId = :jobId")
    Optional<Application> findByUserAndJob(@Param("userId") Long userId, @Param("jobId") Long jobId);
    
    /**
     * Check if user already applied for job
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Application a WHERE a.userId = :userId AND a.jobId = :jobId")
    Boolean hasApplied(@Param("userId") Long userId, @Param("jobId") Long jobId);
    
    /**
     * Find all applications by status
     */
    Page<Application> findByStatus(Application.ApplicationStatus status, Pageable pageable);
    
    /**
     * Find applications with match score >= threshold
     */
    @Query("SELECT a FROM Application a WHERE a.matchScore >= :threshold ORDER BY a.matchScore DESC")
    List<Application> findByMatchScoreGreaterThanOrEqual(@Param("threshold") Double threshold);
    
    /**
     * Find top recommended jobs for user (match score >= 55%)
     */
    @Query("SELECT a FROM Application a WHERE a.userId = :userId AND a.matchScore >= 55 ORDER BY a.matchScore DESC LIMIT 20")
    List<Application> findRecommendedJobsForUser(@Param("userId") Long userId);
    
    /**
     * Count applications by status
     */
    Long countByStatus(Application.ApplicationStatus status);
    
    /**
     * Count applications for job by status
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobId = :jobId AND a.status = :status")
    Long countByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") Application.ApplicationStatus status);
}
