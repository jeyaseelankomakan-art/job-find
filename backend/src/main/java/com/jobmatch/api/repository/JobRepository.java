package com.jobmatch.api.repository;

import com.jobmatch.api.model.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Job Repository
 * Provides database access for Job entity
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    /**
     * Find all published jobs
     */
    @Query("SELECT j FROM Job j WHERE j.status = 'PUBLISHED' ORDER BY j.createdAt DESC")
    Page<Job> findPublished(Pageable pageable);
    
    /**
     * Find jobs by company
     */
    Page<Job> findByCompanyId(Long companyId, Pageable pageable);
    
    /**
     * Find jobs by status
     */
    Page<Job> findByStatus(Job.JobStatus status, Pageable pageable);
    
    /**
     * Search jobs by title
     */
    @Query("SELECT j FROM Job j WHERE j.status = 'PUBLISHED' AND LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY j.createdAt DESC")
    Page<Job> searchByTitle(@Param("query") String query, Pageable pageable);
    
    /**
     * Find jobs by location
     */
    @Query("SELECT j FROM Job j WHERE j.status = 'PUBLISHED' AND (j.remote = true OR j.location LIKE CONCAT('%', :location, '%')) ORDER BY j.createdAt DESC")
    Page<Job> findByLocation(@Param("location") String location, Pageable pageable);
    
    /**
     * Find jobs by experience level
     */
    @Query("SELECT j FROM Job j WHERE j.status = 'PUBLISHED' AND j.experienceLevel = :level ORDER BY j.createdAt DESC")
    Page<Job> findByExperienceLevel(@Param("level") String level, Pageable pageable);
    
    /**
     * Find jobs by job type
     */
    @Query("SELECT j FROM Job j WHERE j.status = 'PUBLISHED' AND j.jobType = :jobType ORDER BY j.createdAt DESC")
    Page<Job> findByJobType(@Param("jobType") String jobType, Pageable pageable);
    
    /**
     * Advanced search for jobs with multiple filters
     */
    @Query("SELECT j FROM Job j WHERE j.status = 'PUBLISHED' " +
            "AND (:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:location IS NULL OR j.remote = true OR j.location LIKE CONCAT('%', :location, '%')) " +
            "AND (:salaryMin IS NULL OR j.salaryMax >= :salaryMin) " +
            "AND (:salaryMax IS NULL OR j.salaryMin <= :salaryMax) " +
            "AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel) " +
            "AND (:jobType IS NULL OR j.jobType = :jobType) " +
            "AND (:companyId IS NULL OR j.companyId = :companyId) " +
            "ORDER BY j.createdAt DESC")
    Page<Job> searchJobs(
            @Param("title") String title,
            @Param("location") String location,
            @Param("salaryMin") Long salaryMin,
            @Param("salaryMax") Long salaryMax,
            @Param("experienceLevel") String experienceLevel,
            @Param("jobType") String jobType,
            @Param("companyId") Long companyId,
            Pageable pageable
    );
    
    /**
     * Count published jobs
     */
    @Query("SELECT COUNT(j) FROM Job j WHERE j.status = 'PUBLISHED'")
    Long countPublished();
}
