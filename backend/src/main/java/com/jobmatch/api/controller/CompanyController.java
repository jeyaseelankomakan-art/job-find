package com.jobmatch.api.controller;

import com.jobmatch.api.model.dto.ApiResponse;
import com.jobmatch.api.model.dto.CompanyRequest;
import com.jobmatch.api.model.entity.Company;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Company REST Controller
 * Handles company-related endpoints
 */
@RestController
@RequestMapping("/api/v1/companies")
@Slf4j
public class CompanyController {
    
    @Autowired
    private CompanyService companyService;
    
    /**
     * GET /api/v1/companies/{id}
     * Get company by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Company>> getCompany(@PathVariable Long id) {
        log.info("GET /companies/{}", id);
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company found", company));
    }
    
    /**
     * GET /api/v1/companies
     * Get all verified companies (paginated)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Company>>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("GET /companies, page: {}, pageSize: {}", page, pageSize);
        Page<Company> companies = companyService.getAllVerifiedCompanies(page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Companies retrieved", companies));
    }
    
    /**
     * GET /api/v1/companies/search
     * Search companies by query
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<Company>>> searchCompanies(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("GET /companies/search, query: {}", query);
        Page<Company> companies = companyService.searchCompanies(query, page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Companies found", companies));
    }
    
    /**
     * GET /api/v1/companies/industry/{industry}
     * Get companies by industry
     */
    @GetMapping("/industry/{industry}")
    public ResponseEntity<ApiResponse<Page<Company>>> getCompaniesByIndustry(
            @PathVariable String industry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("GET /companies/industry/{}", industry);
        Page<Company> companies = companyService.getCompaniesByIndustry(industry, page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Companies found", companies));
    }
    
    /**
     * GET /api/v1/companies/location/{location}
     * Get companies by location
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<ApiResponse<Page<Company>>> getCompaniesByLocation(
            @PathVariable String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("GET /companies/location/{}", location);
        Page<Company> companies = companyService.getCompaniesByLocation(location, page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Companies found", companies));
    }
    
    /**
     * POST /api/v1/companies
     * Create new company
     */
    @PostMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Company>> createCompany(
            @Valid @RequestBody CompanyRequest request,
            Authentication authentication) {
        log.info("POST /companies, company: {}", request.getName());
        
        // Extract user ID from authentication
        Long userId = Long.parseLong(authentication.getName());
        
        Company company = companyService.createCompany(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Company created successfully", company));
    }
    
    /**
     * PUT /api/v1/companies/{id}
     * Update company
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Company>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request,
            Authentication authentication) {
        log.info("PUT /companies/{}", id);
        
        // Verify user is admin of company
        Long userId = Long.parseLong(authentication.getName());
        Boolean isAdmin = companyService.isUserAdminOfCompany(userId, id);
        
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to update this company", null));
        }
        
        Company company = companyService.updateCompany(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company updated successfully", company));
    }
    
    /**
     * DELETE /api/v1/companies/{id}
     * Delete company (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        log.info("DELETE /companies/{}", id);
        companyService.deleteCompany(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company deleted successfully", null));
    }
}
