package com.jobmatch.api.controller;

import com.jobmatch.api.model.dto.ApiResponse;
import com.jobmatch.api.model.entity.Notification;
import com.jobmatch.api.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Notification REST Controller
 * Handles notification endpoints
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * GET /api/v1/notifications
     * Get all notifications for current user
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Page<Notification>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication) {
        log.info("GET /notifications");
        
        Long userId = Long.parseLong(authentication.getName());
        Page<Notification> notifications = notificationService.getUserNotifications(userId, page, pageSize);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved", notifications));
    }
    
    /**
     * GET /api/v1/notifications/unread
     * Get unread notifications for current user
     */
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Page<Notification>>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication) {
        log.info("GET /notifications/unread");
        
        Long userId = Long.parseLong(authentication.getName());
        Page<Notification> notifications = notificationService.getUnreadNotifications(userId, page, pageSize);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread notifications retrieved", notifications));
    }
    
    /**
     * GET /api/v1/notifications/unread-count
     * Get count of unread notifications
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
        log.info("GET /notifications/unread-count");
        
        Long userId = Long.parseLong(authentication.getName());
        Long count = notificationService.countUnreadNotifications(userId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread count retrieved", count));
    }
    
    /**
     * PUT /api/v1/notifications/{id}/read
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("PUT /notifications/{}/read", id);
        
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read", notification));
    }
    
    /**
     * PUT /api/v1/notifications/read-all
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        log.info("PUT /notifications/read-all");
        
        Long userId = Long.parseLong(authentication.getName());
        notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read", null));
    }
    
    /**
     * DELETE /api/v1/notifications/{id}
     * Delete notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("DELETE /notifications/{}", id);
        
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification deleted", null));
    }
}
