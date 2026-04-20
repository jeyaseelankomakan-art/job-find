package com.jobmatch.api.repository;

import com.jobmatch.api.model.entity.AdminLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * AdminLog Repository
 * Provides database access for AdminLog entity
 */
@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
    
    /**
     * Find all admin logs
     */
    Page<AdminLog> findAll(Pageable pageable);
    
    /**
     * Find logs by admin ID
     */
    Page<AdminLog> findByAdminId(Long adminId, Pageable pageable);
    
    /**
     * Find logs by action
     */
    Page<AdminLog> findByAction(AdminLog.AdminAction action, Pageable pageable);
    
    /**
     * Find logs by target ID
     */
    @Query("SELECT l FROM AdminLog l WHERE l.targetId = :targetId ORDER BY l.createdAt DESC")
    Page<AdminLog> findByTargetId(@Param("targetId") Long targetId, Pageable pageable);
    
    /**
     * Count logs by action
     */
    Long countByAction(AdminLog.AdminAction action);
}
