package com.jobmatch.api.service;

import com.jobmatch.api.model.entity.Notification;
import com.jobmatch.api.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Notification Service
 * Business logic for notifications with async event handlers
 */
@Service
@Transactional
@Slf4j
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Get all notifications for user
     */
    public Page<Notification> getUserNotifications(Long userId, int page, int pageSize) {
        log.debug("Getting notifications for user: {}", userId);
        Pageable pageable = PageRequest.of(page, pageSize);
        return notificationRepository.findByUserId(userId, pageable);
    }
    
    /**
     * Get unread notifications for user
     */
    public Page<Notification> getUnreadNotifications(Long userId, int page, int pageSize) {
        log.debug("Getting unread notifications for user: {}", userId);
        Pageable pageable = PageRequest.of(page, pageSize);
        return notificationRepository.findUnreadByUserId(userId, pageable);
    }
    
    /**
     * Count unread notifications for user
     */
    public Long countUnreadNotifications(Long userId) {
        log.debug("Counting unread notifications for user: {}", userId);
        return notificationRepository.countUnreadForUser(userId);
    }
    
    /**
     * Mark notification as read
     */
    public Notification markAsRead(Long notificationId) {
        log.debug("Marking notification as read: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setIsRead(true);
        notification.setReadAt(new Date());
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Mark all notifications as read for user
     */
    public void markAllAsRead(Long userId) {
        log.debug("Marking all notifications as read for user: {}", userId);
        
        // Get all unread notifications and mark them as read
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId, pageable);
        
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(new Date());
            notificationRepository.save(notification);
        });
    }
    
    /**
     * Delete notification
     */
    public void deleteNotification(Long notificationId) {
        log.debug("Deleting notification: {}", notificationId);
        notificationRepository.deleteById(notificationId);
    }
    
    // ==================== Async Event Handlers ====================
    
    /**
     * Notify when application is submitted
     */
    @Async
    public void notifyApplicationSubmitted(Long jobId, Long userId, Long applicationId) {
        log.info("Sending application submitted notification for job: {}, user: {}", jobId, userId);
        
        // Notification to job poster (company admin)
        // TODO: Query to get company admin for job and send notification
        
        // Notification to applicant
        Notification notification = Notification.builder()
                .userId(userId)
                .type(Notification.NotificationType.APPLICATION_SUBMITTED)
                .title("Application Submitted")
                .message("Your application has been submitted successfully")
                .relatedId(applicationId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * Notify when application is accepted
     */
    @Async
    public void notifyApplicationAccepted(Long applicationId, Long userId) {
        log.info("Sending application accepted notification for user: {}", userId);
        
        Notification notification = Notification.builder()
                .userId(userId)
                .type(Notification.NotificationType.APPLICATION_ACCEPTED)
                .title("Application Accepted")
                .message("Congratulations! Your application has been accepted")
                .relatedId(applicationId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * Notify when application is rejected
     */
    @Async
    public void notifyApplicationRejected(Long applicationId, Long userId) {
        log.info("Sending application rejected notification for user: {}", userId);
        
        Notification notification = Notification.builder()
                .userId(userId)
                .type(Notification.NotificationType.APPLICATION_REJECTED)
                .title("Application Rejected")
                .message("Your application has been rejected")
                .relatedId(applicationId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * Notify when company is verified
     */
    @Async
    public void notifyCompanyVerified(Long companyId) {
        log.info("Sending company verified notification for company: {}", companyId);
        
        // TODO: Query to get company admin and send notification
        
        Notification notification = Notification.builder()
                .type(Notification.NotificationType.COMPANY_VERIFIED)
                .title("Company Verified")
                .message("Your company has been verified by admin")
                .relatedId(companyId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * Notify when user is blocked
     */
    @Async
    public void notifyUserBlocked(Long userId) {
        log.info("Sending user blocked notification for user: {}", userId);
        
        Notification notification = Notification.builder()
                .userId(userId)
                .type(Notification.NotificationType.USER_BLOCKED)
                .title("Account Blocked")
                .message("Your account has been blocked by administrators")
                .relatedId(userId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * Notify when job is published
     */
    @Async
    public void notifyJobPublished(Long jobId, Long companyId) {
        log.info("Sending job published notification for job: {}", jobId);
        
        // TODO: Query to get matching job seekers and send notifications
        
        Notification notification = Notification.builder()
                .type(Notification.NotificationType.JOB_PUBLISHED)
                .title("New Job Posted")
                .message("A new job that matches your profile has been posted")
                .relatedId(jobId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * Notify when job is closed
     */
    @Async
    public void notifyJobClosed(Long jobId) {
        log.info("Sending job closed notification for job: {}", jobId);
        
        // TODO: Query to get applicants and send notifications
        
        Notification notification = Notification.builder()
                .type(Notification.NotificationType.JOB_CLOSED)
                .title("Job Closed")
                .message("A job you applied for has been closed")
                .relatedId(jobId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }
}
