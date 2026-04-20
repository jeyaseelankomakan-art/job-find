package com.jobmatch.api.service;

import com.jobmatch.api.model.entity.Notification;
import com.jobmatch.api.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService
 */
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @InjectMocks
    private NotificationService notificationService;
    
    private Notification testNotification;
    
    @BeforeEach
    public void setUp() {
        testNotification = Notification.builder()
                .id(1L)
                .userId(1L)
                .type(Notification.NotificationType.APPLICATION_SUBMITTED)
                .title("Application Submitted")
                .message("Your application has been submitted")
                .isRead(false)
                .build();
    }
    
    @Test
    public void testGetUserNotifications() {
        // Arrange
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);
        Page<Notification> page = new PageImpl<>(notifications);
        
        when(notificationRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<Notification> result = notificationService.getUserNotifications(1L, 0, 20);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        verify(notificationRepository).findByUserId(eq(1L), any(Pageable.class));
    }
    
    @Test
    public void testGetUnreadNotifications() {
        // Arrange
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);
        Page<Notification> page = new PageImpl<>(notifications);
        
        when(notificationRepository.findUnreadByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<Notification> result = notificationService.getUnreadNotifications(1L, 0, 20);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        verify(notificationRepository).findUnreadByUserId(eq(1L), any(Pageable.class));
    }
    
    @Test
    public void testCountUnreadNotifications() {
        // Arrange
        when(notificationRepository.countUnreadForUser(1L)).thenReturn(5L);
        
        // Act
        Long count = notificationService.countUnreadNotifications(1L);
        
        // Assert
        assertEquals(5L, count);
        
        verify(notificationRepository).countUnreadForUser(1L);
    }
    
    @Test
    public void testMarkAsRead() {
        // Arrange
        when(notificationRepository.findById(testNotification.getId())).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // Act
        Notification result = notificationService.markAsRead(testNotification.getId());
        
        // Assert
        assertNotNull(result);
        
        verify(notificationRepository).findById(testNotification.getId());
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    public void testDeleteNotification() {
        // Arrange
        doNothing().when(notificationRepository).deleteById(testNotification.getId());
        
        // Act
        notificationService.deleteNotification(testNotification.getId());
        
        // Assert
        verify(notificationRepository).deleteById(testNotification.getId());
    }
}
