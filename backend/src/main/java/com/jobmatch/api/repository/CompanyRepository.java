package com.jobmatch.api.repository;

import com.jobmatch.api.model.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Company Repository
 * Provides database access for Company entity
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    /**
     * Find company by name
     */
    Optional<Company> findByName(String name);
    
    /**
     * Find all verified companies
     */
    @Query("SELECT c FROM Company c WHERE c.verified = true")
    Page<Company> findAllVerified(Pageable pageable);
    
    /**
     * Search companies by name
     */
    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.createdAt DESC")
    Page<Company> searchByName(@Param("name") String name, Pageable pageable);
    
    /**
     * Find companies by industry
     */
    Page<Company> findByIndustry(String industry, Pageable pageable);
    
    /**
     * Find companies by location
     */
    @Query("SELECT c FROM Company c WHERE c.location LIKE CONCAT('%', :location, '%') ORDER BY c.name ASC")
    Page<Company> findByLocation(@Param("location") String location, Pageable pageable);
}
