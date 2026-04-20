package com.jobmatch.api.repository;

import com.jobmatch.api.model.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserSkill Repository
 * Provides database access for UserSkill entity
 */
@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    
    /**
     * Find all skills for a user
     */
    @Query("SELECT us FROM UserSkill us WHERE us.userId = :userId")
    List<UserSkill> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find user skill by user ID and skill ID
     */
    @Query("SELECT us FROM UserSkill us WHERE us.userId = :userId AND us.skillId = :skillId")
    Optional<UserSkill> findByUserIdAndSkillId(@Param("userId") Long userId, @Param("skillId") Long skillId);
    
    /**
     * Check if user has a skill
     */
    @Query("SELECT CASE WHEN COUNT(us) > 0 THEN true ELSE false END FROM UserSkill us WHERE us.userId = :userId AND us.skillId = :skillId")
    Boolean hasSkill(@Param("userId") Long userId, @Param("skillId") Long skillId);
    
    /**
     * Count skills for a user
     */
    @Query("SELECT COUNT(us) FROM UserSkill us WHERE us.userId = :userId")
    Integer countByUserId(@Param("userId") Long userId);
    
    /**
     * Delete user skill
     */
    void deleteByUserIdAndSkillId(Long userId, Long skillId);
}
