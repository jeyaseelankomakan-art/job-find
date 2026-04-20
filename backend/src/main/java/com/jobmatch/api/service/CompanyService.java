package com.jobmatch.api.service;

import com.jobmatch.api.exception.ResourceNotFoundException;
import com.jobmatch.api.model.dto.CompanyRequest;
import com.jobmatch.api.model.entity.Company;
import com.jobmatch.api.model.entity.CompanyAdmin;
import com.jobmatch.api.repository.CompanyAdminRepository;
import com.jobmatch.api.repository.CompanyRepository;
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
 * Company Service
 * Business logic for company operations
 */
@Service
@Transactional
@Slf4j
public class CompanyService {
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private CompanyAdminRepository companyAdminRepository;
    
    /**
     * Get company by ID
     */
    public Company getCompanyById(Long companyId) {
        log.debug("Getting company by ID: {}", companyId);
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));
    }
    
    /**
     * Get company by name
     */
    public Company getCompanyByName(String name) {
        log.debug("Getting company by name: {}", name);
        return companyRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with name: " + name));
    }
    
    /**
     * Get all verified companies
     */
    public Page<Company> getAllVerifiedCompanies(int page, int pageSize) {
        log.debug("Getting all verified companies, page: {}, size: {}", page, pageSize);
        Pageable pageable = PageRequest.of(page, pageSize);
        return companyRepository.findAllVerified(pageable);
    }
    
    /**
     * Search companies by name
     */
    public Page<Company> searchCompanies(String query, int page, int pageSize) {
        log.debug("Searching companies with query: {}", query);
        Pageable pageable = PageRequest.of(page, pageSize);
        return companyRepository.searchByName(query, pageable);
    }
    
    /**
     * Get companies by industry
     */
    public Page<Company> getCompaniesByIndustry(String industry, int page, int pageSize) {
        log.debug("Getting companies by industry: {}", industry);
        Pageable pageable = PageRequest.of(page, pageSize);
        return companyRepository.findByIndustry(industry, pageable);
    }
    
    /**
     * Get companies by location
     */
    public Page<Company> getCompaniesByLocation(String location, int page, int pageSize) {
        log.debug("Getting companies by location: {}", location);
        Pageable pageable = PageRequest.of(page, pageSize);
        return companyRepository.findByLocation(location, pageable);
    }
    
    /**
     * Create new company
     */
    public Company createCompany(CompanyRequest request, Long userId) {
        log.info("Creating new company: {}", request.getName());
        
        Company company = Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .companySize(request.getCompanySize())
                .location(request.getLocation())
                .logoUrl(request.getLogoUrl())
                .verified(false)
                .build();
        
        company = companyRepository.save(company);
        
        // Add creator as owner
        CompanyAdmin admin = CompanyAdmin.builder()
                .companyId(company.getId())
                .userId(userId)
                .isOwner(true)
                .build();
        companyAdminRepository.save(admin);
        
        log.info("Company created with ID: {}", company.getId());
        return company;
    }
    
    /**
     * Update company
     */
    public Company updateCompany(Long companyId, CompanyRequest request) {
        log.info("Updating company: {}", companyId);
        
        Company company = getCompanyById(companyId);
        
        if (request.getName() != null) company.setName(request.getName());
        if (request.getDescription() != null) company.setDescription(request.getDescription());
        if (request.getWebsite() != null) company.setWebsite(request.getWebsite());
        if (request.getIndustry() != null) company.setIndustry(request.getIndustry());
        if (request.getCompanySize() != null) company.setCompanySize(request.getCompanySize());
        if (request.getLocation() != null) company.setLocation(request.getLocation());
        if (request.getLogoUrl() != null) company.setLogoUrl(request.getLogoUrl());
        
        company = companyRepository.save(company);
        log.info("Company updated: {}", companyId);
        return company;
    }
    
    /**
     * Verify company (admin only)
     */
    public Company verifyCompany(Long companyId, Long adminId) {
        log.info("Verifying company: {} by admin: {}", companyId, adminId);
        
        Company company = getCompanyById(companyId);
        company.setVerified(true);
        company.setVerifiedBy(adminId);
        company.setVerificationDate(new Date());
        
        company = companyRepository.save(company);
        log.info("Company verified: {}", companyId);
        return company;
    }
    
    /**
     * Delete company
     */
    public void deleteCompany(Long companyId) {
        log.info("Deleting company: {}", companyId);
        Company company = getCompanyById(companyId);
        companyRepository.delete(company);
        log.info("Company deleted: {}", companyId);
    }
    
    /**
     * Add admin to company
     */
    public CompanyAdmin addAdmin(Long companyId, Long userId, Boolean isOwner) {
        log.info("Adding admin {} to company {}", userId, companyId);
        
        getCompanyById(companyId); // Verify company exists
        
        CompanyAdmin admin = CompanyAdmin.builder()
                .companyId(companyId)
                .userId(userId)
                .isOwner(isOwner != null && isOwner)
                .build();
        
        return companyAdminRepository.save(admin);
    }
    
    /**
     * Get all companies for user
     */
    public List<CompanyAdmin> getUserCompanies(Long userId) {
        log.debug("Getting companies for user: {}", userId);
        return companyAdminRepository.findCompaniesByUserId(userId);
    }
    
    /**
     * Check if user is admin of company
     */
    public Boolean isUserAdminOfCompany(Long userId, Long companyId) {
        log.debug("Checking if user {} is admin of company {}", userId, companyId);
        return companyAdminRepository.isUserAdminOfCompany(userId, companyId);
    }
}
