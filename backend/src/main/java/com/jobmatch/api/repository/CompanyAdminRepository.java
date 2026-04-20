package com.jobmatch.api.repository;

import com.jobmatch.api.model.entity.CompanyAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CompanyAdmin Repository
 * Provides database access for CompanyAdmin entity
 */
@Repository
public interface CompanyAdminRepository extends JpaRepository<CompanyAdmin, Long> {
    
    /**
     * Find all admins for a company
     */
    List<CompanyAdmin> findByCompanyId(Long companyId);
    
    /**
     * Find all companies for a user
     */
    @Query("SELECT ca FROM CompanyAdmin ca WHERE ca.userId = :userId")
    List<CompanyAdmin> findCompaniesByUserId(@Param("userId") Long userId);
    
    /**
     * Check if user is admin of company
     */
    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END FROM CompanyAdmin ca WHERE ca.userId = :userId AND ca.companyId = :companyId")
    Boolean isUserAdminOfCompany(@Param("userId") Long userId, @Param("companyId") Long companyId);
    
    /**
     * Find company owner
     */
    @Query("SELECT ca FROM CompanyAdmin ca WHERE ca.companyId = :companyId AND ca.isOwner = true")
    Optional<CompanyAdmin> findOwner(@Param("companyId") Long companyId);
}
