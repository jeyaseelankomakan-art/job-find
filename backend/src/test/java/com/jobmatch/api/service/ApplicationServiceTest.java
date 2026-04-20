package com.jobmatch.api.service;

import com.jobmatch.api.exception.DuplicateResourceException;
import com.jobmatch.api.exception.ResourceNotFoundException;
import com.jobmatch.api.model.dto.ApplicationRequest;
import com.jobmatch.api.model.entity.Application;
import com.jobmatch.api.repository.ApplicationRepository;
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
 * Unit tests for ApplicationService
 */
@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {
    
    @Mock
    private ApplicationRepository applicationRepository;
    
    @Mock
    private MatchingService matchingService;
    
    @Mock
    private JobService jobService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private ApplicationService applicationService;
    
    private Application testApplication;
    private ApplicationRequest testRequest;
    
    @BeforeEach
    public void setUp() {
        testApplication = Application.builder()
                .id(1L)
                .userId(1L)
                .jobId(1L)
                .coverLetter("I am interested in this position")
                .matchScore(75.0)
                .status(Application.ApplicationStatus.PENDING)
                .build();
        
        testRequest = ApplicationRequest.builder()
                .jobId(1L)
                .coverLetter("I am interested in this position")
                .build();
    }
    
    @Test
    public void testSubmitApplicationSuccess() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(new com.jobmatch.api.model.entity.User());
        when(jobService.getJobById(1L)).thenReturn(new com.jobmatch.api.model.entity.Job());
        when(applicationRepository.hasApplied(1L, 1L)).thenReturn(false);
        when(matchingService.calculateMatchScore(1L, 1L)).thenReturn(75.0);
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);
        
        // Act
        Application result = applicationService.submitApplication(1L, testRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(75.0, result.getMatchScore());
        assertEquals(Application.ApplicationStatus.PENDING, result.getStatus());
        
        verify(userService).getUserById(1L);
        verify(jobService).getJobById(1L);
        verify(applicationRepository).hasApplied(1L, 1L);
        verify(matchingService).calculateMatchScore(1L, 1L);
        verify(applicationRepository).save(any(Application.class));
    }
    
    @Test
    public void testSubmitApplicationDuplicate() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(new com.jobmatch.api.model.entity.User());
        when(jobService.getJobById(1L)).thenReturn(new com.jobmatch.api.model.entity.Job());
        when(applicationRepository.hasApplied(1L, 1L)).thenReturn(true);
        
        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            applicationService.submitApplication(1L, testRequest);
        });
        
        verify(applicationRepository).hasApplied(1L, 1L);
        verify(applicationRepository, never()).save(any());
    }
    
    @Test
    public void testGetApplicationByIdSuccess() {
        // Arrange
        when(applicationRepository.findById(testApplication.getId())).thenReturn(Optional.of(testApplication));
        
        // Act
        Application result = applicationService.getApplicationById(testApplication.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(testApplication.getId(), result.getId());
        
        verify(applicationRepository).findById(testApplication.getId());
    }
    
    @Test
    public void testGetApplicationByIdNotFound() {
        // Arrange
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            applicationService.getApplicationById(999L);
        });
        
        verify(applicationRepository).findById(999L);
    }
    
    @Test
    public void testGetUserApplications() {
        // Arrange
        List<Application> applications = new ArrayList<>();
        applications.add(testApplication);
        Page<Application> page = new PageImpl<>(applications);
        
        when(applicationRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<Application> result = applicationService.getUserApplications(1L, 0, 20);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        verify(applicationRepository).findByUserId(eq(1L), any(Pageable.class));
    }
    
    @Test
    public void testAcceptApplication() {
        // Arrange
        when(applicationRepository.findById(testApplication.getId())).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);
        
        // Act
        Application result = applicationService.acceptApplication(testApplication.getId(), 1L);
        
        // Assert
        assertNotNull(result);
        
        verify(applicationRepository).findById(testApplication.getId());
        verify(applicationRepository).save(any(Application.class));
    }
    
    @Test
    public void testRejectApplication() {
        // Arrange
        when(applicationRepository.findById(testApplication.getId())).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);
        
        // Act
        Application result = applicationService.rejectApplication(testApplication.getId(), 1L, "Not qualified");
        
        // Assert
        assertNotNull(result);
        
        verify(applicationRepository).findById(testApplication.getId());
        verify(applicationRepository).save(any(Application.class));
    }
    
    @Test
    public void testWithdrawApplication() {
        // Arrange
        when(applicationRepository.findById(testApplication.getId())).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);
        
        // Act
        Application result = applicationService.withdrawApplication(testApplication.getId());
        
        // Assert
        assertNotNull(result);
        
        verify(applicationRepository).findById(testApplication.getId());
        verify(applicationRepository).save(any(Application.class));
    }
}
