package com.jobmatch.api.repository;

import com.jobmatch.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository
 * Provides database access for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if email exists
     */
    Boolean existsByEmail(String email);
    
    /**
     * Find all active users
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findAllActive();
    
    /**
     * Find job seekers by location
     */
    @Query("SELECT u FROM User u WHERE u.role = 'JOB_SEEKER' AND u.status = 'ACTIVE' AND u.location = :location")
    List<User> findJobSeekersByLocation(@Param("location") String location);
}
