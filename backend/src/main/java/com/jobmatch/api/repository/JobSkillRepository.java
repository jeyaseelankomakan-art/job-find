package com.jobmatch.api.repository;

import com.jobmatch.api.model.entity.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JobSkill Repository
 * Provides database access for JobSkill entity
 */
@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
    
    /**
     * Find all skills for a job
     */
    @Query("SELECT js FROM JobSkill js WHERE js.jobId = :jobId")
    List<JobSkill> findByJobId(@Param("jobId") Long jobId);
    
    /**
     * Find mandatory skills for a job
     */
    @Query("SELECT js FROM JobSkill js WHERE js.jobId = :jobId AND js.mandatory = true")
    List<JobSkill> findMandatorySkillsByJobId(@Param("jobId") Long jobId);
    
    /**
     * Find job skill by job ID and skill ID
     */
    @Query("SELECT js FROM JobSkill js WHERE js.jobId = :jobId AND js.skillId = :skillId")
    Optional<JobSkill> findByJobIdAndSkillId(@Param("jobId") Long jobId, @Param("skillId") Long skillId);
    
    /**
     * Count skills for a job
     */
    @Query("SELECT COUNT(js) FROM JobSkill js WHERE js.jobId = :jobId")
    Integer countByJobId(@Param("jobId") Long jobId);
    
    /**
     * Delete all skills for a job
     */
    void deleteByJobId(Long jobId);
}
