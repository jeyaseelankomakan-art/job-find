package com.jobmatch.api.service;

import com.jobmatch.api.model.entity.AdminLog;
import com.jobmatch.api.model.entity.Company;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.repository.AdminLogRepository;
import com.jobmatch.api.repository.CompanyRepository;
import com.jobmatch.api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Admin Service
 * Business logic for admin operations
 */
@Service
@Transactional
@Slf4j
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private AdminLogRepository adminLogRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Block user
     */
    public User blockUser(Long userId, Long adminId, String reason) {
        log.info("Blocking user: {} by admin: {}", userId, adminId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setStatus(User.UserStatus.BLOCKED);
        user = userRepository.save(user);
        
        // Log admin action
        logAdminAction(adminId, AdminLog.AdminAction.USER_BLOCKED, "User", userId, 
                "Blocked user: " + user.getEmail() + ". Reason: " + reason);
        
        // Send notification
        notificationService.notifyUserBlocked(userId);
        
        log.info("User blocked: {}", userId);
        return user;
    }
    
    /**
     * Unblock user
     */
    public User unblockUser(Long userId, Long adminId) {
        log.info("Unblocking user: {} by admin: {}", userId, adminId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setStatus(User.UserStatus.ACTIVE);
        user = userRepository.save(user);
        
        // Log admin action
        logAdminAction(adminId, AdminLog.AdminAction.USER_UNBLOCKED, "User", userId,
                "Unblocked user: " + user.getEmail());
        
        log.info("User unblocked: {}", userId);
        return user;
    }
    
    /**
     * Verify company
     */
    public Company verifyCompany(Long companyId, Long adminId) {
        log.info("Verifying company: {} by admin: {}", companyId, adminId);
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        company.setVerified(true);
        company.setVerifiedBy(adminId);
        company.setVerificationDate(new Date());
        company = companyRepository.save(company);
        
        // Log admin action
        logAdminAction(adminId, AdminLog.AdminAction.COMPANY_VERIFIED, "Company", companyId,
                "Verified company: " + company.getName());
        
        // Send notification (to company admin)
        notificationService.notifyCompanyVerified(companyId);
        
        log.info("Company verified: {}", companyId);
        return company;
    }
    
    /**
     * Reject company verification
     */
    public void rejectCompany(Long companyId, Long adminId, String reason) {
        log.info("Rejecting company: {} by admin: {}", companyId, adminId);
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        // Log admin action
        logAdminAction(adminId, AdminLog.AdminAction.COMPANY_REJECTED, "Company", companyId,
                "Rejected company: " + company.getName() + ". Reason: " + reason);
        
        log.info("Company rejected: {}", companyId);
    }
    
    /**
     * Log admin action for audit trail
     */
    private void logAdminAction(Long adminId, AdminLog.AdminAction action, 
                               String targetType, Long targetId, String description) {
        log.debug("Logging admin action: {}", action);
        
        AdminLog log = AdminLog.builder()
                .adminId(adminId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .build();
        
        adminLogRepository.save(log);
    }
    
    /**
     * Get admin logs (paginated)
     */
    public Page<AdminLog> getAdminLogs(int page, int pageSize) {
        log.debug("Getting admin logs, page: {}, size: {}", page, pageSize);
        Pageable pageable = PageRequest.of(page, pageSize);
        return adminLogRepository.findAll(pageable);
    }
    
    /**
     * Get admin logs by admin ID
     */
    public Page<AdminLog> getAdminLogsByAdminId(Long adminId, int page, int pageSize) {
        log.debug("Getting admin logs for admin: {}", adminId);
        Pageable pageable = PageRequest.of(page, pageSize);
        return adminLogRepository.findByAdminId(adminId, pageable);
    }
    
    /**
     * Get admin logs by action
     */
    public Page<AdminLog> getAdminLogsByAction(AdminLog.AdminAction action, int page, int pageSize) {
        log.debug("Getting admin logs by action: {}", action);
        Pageable pageable = PageRequest.of(page, pageSize);
        return adminLogRepository.findByAction(action, pageable);
    }
}
