package com.jobmatch.api.controller;

import com.jobmatch.api.model.dto.ApiResponse;
import com.jobmatch.api.model.entity.AdminLog;
import com.jobmatch.api.model.entity.Company;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin REST Controller
 * Handles admin-only operations
 */
@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    /**
     * POST /api/v1/admin/users/{userId}/block
     * Block user
     */
    @PostMapping("/users/{userId}/block")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<User>> blockUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("POST /admin/users/{}/block", userId);
        
        Long adminId = Long.parseLong(authentication.getName());
        User user = adminService.blockUser(userId, adminId, reason);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "User blocked successfully", user));
    }
    
    /**
     * POST /api/v1/admin/users/{userId}/unblock
     * Unblock user
     */
    @PostMapping("/users/{userId}/unblock")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<User>> unblockUser(
            @PathVariable Long userId,
            Authentication authentication) {
        log.info("POST /admin/users/{}/unblock", userId);
        
        Long adminId = Long.parseLong(authentication.getName());
        User user = adminService.unblockUser(userId, adminId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "User unblocked successfully", user));
    }
    
    /**
     * POST /api/v1/admin/companies/{companyId}/verify
     * Verify company
     */
    @PostMapping("/companies/{companyId}/verify")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Company>> verifyCompany(
            @PathVariable Long companyId,
            Authentication authentication) {
        log.info("POST /admin/companies/{}/verify", companyId);
        
        Long adminId = Long.parseLong(authentication.getName());
        Company company = adminService.verifyCompany(companyId, adminId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Company verified successfully", company));
    }
    
    /**
     * POST /api/v1/admin/companies/{companyId}/reject
     * Reject company
     */
    @PostMapping("/companies/{companyId}/reject")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectCompany(
            @PathVariable Long companyId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("POST /admin/companies/{}/reject", companyId);
        
        Long adminId = Long.parseLong(authentication.getName());
        adminService.rejectCompany(companyId, adminId, reason);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Company rejected", null));
    }
    
    /**
     * GET /api/v1/admin/logs
     * Get admin logs
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminLog>>> getAdminLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        log.info("GET /admin/logs, page: {}, pageSize: {}", page, pageSize);
        
        Page<AdminLog> logs = adminService.getAdminLogs(page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Admin logs retrieved", logs));
    }
    
    /**
     * GET /api/v1/admin/logs/admin/{adminId}
     * Get admin logs by admin
     */
    @GetMapping("/logs/admin/{adminId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminLog>>> getAdminLogsByAdmin(
            @PathVariable Long adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        log.info("GET /admin/logs/admin/{}, page: {}, pageSize: {}", adminId, page, pageSize);
        
        Page<AdminLog> logs = adminService.getAdminLogsByAdminId(adminId, page, pageSize);
        return ResponseEntity.ok(new ApiResponse<>(true, "Admin logs retrieved", logs));
    }
    
    /**
     * GET /api/v1/admin/logs/action/{action}
     * Get admin logs by action
     */
    @GetMapping("/logs/action/{action}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminLog>>> getAdminLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        log.info("GET /admin/logs/action/{}, page: {}, pageSize: {}", action, page, pageSize);
        
        AdminLog.AdminAction adminAction = AdminLog.AdminAction.valueOf(action.toUpperCase());
        Page<AdminLog> logs = adminService.getAdminLogsByAction(adminAction, page, pageSize);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Admin logs retrieved", logs));
    }
}
