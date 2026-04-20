package com.jobmatch.api.service;

import com.jobmatch.api.model.entity.AdminLog;
import com.jobmatch.api.model.entity.Company;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.repository.AdminLogRepository;
import com.jobmatch.api.repository.CompanyRepository;
import com.jobmatch.api.repository.UserRepository;
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
 * Unit tests for AdminService
 */
@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CompanyRepository companyRepository;
    
    @Mock
    private AdminLogRepository adminLogRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private AdminService adminService;
    
    private User testUser;
    private Company testCompany;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        testCompany = Company.builder()
                .id(1L)
                .name("Tech Corp")
                .verified(false)
                .build();
    }
    
    @Test
    public void testBlockUser() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(adminLogRepository.save(any(AdminLog.class))).thenReturn(new AdminLog());
        
        // Act
        User result = adminService.blockUser(testUser.getId(), 1L, "Violation");
        
        // Assert
        assertNotNull(result);
        
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(any(User.class));
        verify(adminLogRepository).save(any(AdminLog.class));
        verify(notificationService).notifyUserBlocked(testUser.getId());
    }
    
    @Test
    public void testUnblockUser() {
        // Arrange
        testUser.setStatus(User.UserStatus.BLOCKED);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(adminLogRepository.save(any(AdminLog.class))).thenReturn(new AdminLog());
        
        // Act
        User result = adminService.unblockUser(testUser.getId(), 1L);
        
        // Assert
        assertNotNull(result);
        
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(any(User.class));
        verify(adminLogRepository).save(any(AdminLog.class));
    }
    
    @Test
    public void testVerifyCompany() {
        // Arrange
        when(companyRepository.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);
        when(adminLogRepository.save(any(AdminLog.class))).thenReturn(new AdminLog());
        
        // Act
        Company result = adminService.verifyCompany(testCompany.getId(), 1L);
        
        // Assert
        assertNotNull(result);
        
        verify(companyRepository).findById(testCompany.getId());
        verify(companyRepository).save(any(Company.class));
        verify(adminLogRepository).save(any(AdminLog.class));
        verify(notificationService).notifyCompanyVerified(testCompany.getId());
    }
    
    @Test
    public void testGetAdminLogs() {
        // Arrange
        List<AdminLog> logs = new ArrayList<>();
        Page<AdminLog> page = new PageImpl<>(logs);
        
        when(adminLogRepository.findAll(any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<AdminLog> result = adminService.getAdminLogs(0, 50);
        
        // Assert
        assertNotNull(result);
        
        verify(adminLogRepository).findAll(any(Pageable.class));
    }
}
