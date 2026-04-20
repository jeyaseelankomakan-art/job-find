package com.jobmatch.api.repository;

import com.jobmatch.api.model.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Skill Repository
 * Provides database access for Skill entity
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    /**
     * Find skill by name
     */
    Optional<Skill> findByName(String name);
    
    /**
     * Find skills by category
     */
    List<Skill> findByCategory(String category);
    
    /**
     * Search skills by name (case-insensitive)
     */
    @Query("SELECT s FROM Skill s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY s.popularity DESC")
    List<Skill> searchByName(@Param("name") String name);
    
    /**
     * Get top popular skills
     */
    @Query("SELECT s FROM Skill s ORDER BY s.popularity DESC LIMIT :limit")
    List<Skill> getTopPopularSkills(@Param("limit") int limit);
}
