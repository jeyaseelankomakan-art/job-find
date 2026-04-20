package com.jobmatch.api.service;

import com.jobmatch.api.exception.ResourceNotFoundException;
import com.jobmatch.api.model.dto.CompanyRequest;
import com.jobmatch.api.model.entity.Company;
import com.jobmatch.api.model.entity.CompanyAdmin;
import com.jobmatch.api.repository.CompanyAdminRepository;
import com.jobmatch.api.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CompanyService
 */
@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {
    
    @Mock
    private CompanyRepository companyRepository;
    
    @Mock
    private CompanyAdminRepository companyAdminRepository;
    
    @InjectMocks
    private CompanyService companyService;
    
    private Company testCompany;
    private CompanyRequest testRequest;
    
    @BeforeEach
    public void setUp() {
        testCompany = Company.builder()
                .id(1L)
                .name("Tech Corp")
                .description("Technology Company")
                .website("https://techcorp.com")
                .industry("Technology")
                .companySize("large")
                .location("San Francisco, CA")
                .verified(true)
                .build();
        
        testRequest = CompanyRequest.builder()
                .name("Tech Corp")
                .description("Technology Company")
                .website("https://techcorp.com")
                .industry("Technology")
                .companySize("large")
                .location("San Francisco, CA")
                .build();
    }
    
    @Test
    public void testGetCompanyByIdSuccess() {
        // Arrange
        when(companyRepository.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
        
        // Act
        Company result = companyService.getCompanyById(testCompany.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(testCompany.getId(), result.getId());
        assertEquals("Tech Corp", result.getName());
        
        verify(companyRepository).findById(testCompany.getId());
    }
    
    @Test
    public void testGetCompanyByIdNotFound() {
        // Arrange
        when(companyRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            companyService.getCompanyById(999L);
        });
        
        verify(companyRepository).findById(999L);
    }
    
    @Test
    public void testGetCompanyByNameSuccess() {
        // Arrange
        when(companyRepository.findByName("Tech Corp")).thenReturn(Optional.of(testCompany));
        
        // Act
        Company result = companyService.getCompanyByName("Tech Corp");
        
        // Assert
        assertNotNull(result);
        assertEquals("Tech Corp", result.getName());
        
        verify(companyRepository).findByName("Tech Corp");
    }
    
    @Test
    public void testGetAllVerifiedCompanies() {
        // Arrange
        List<Company> companies = new ArrayList<>();
        companies.add(testCompany);
        Page<Company> page = new PageImpl<>(companies);
        
        when(companyRepository.findAllVerified(any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<Company> result = companyService.getAllVerifiedCompanies(0, 20);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        verify(companyRepository).findAllVerified(any(Pageable.class));
    }
    
    @Test
    public void testCreateCompany() {
        // Arrange
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);
        when(companyAdminRepository.save(any(CompanyAdmin.class)))
                .thenReturn(CompanyAdmin.builder().id(1L).companyId(testCompany.getId()).userId(1L).isOwner(true).build());
        
        // Act
        Company result = companyService.createCompany(testRequest, 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("Tech Corp", result.getName());
        
        verify(companyRepository).save(any(Company.class));
        verify(companyAdminRepository).save(any(CompanyAdmin.class));
    }
    
    @Test
    public void testUpdateCompany() {
        // Arrange
        when(companyRepository.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);
        
        CompanyRequest updateRequest = new CompanyRequest();
        updateRequest.setName("Updated Tech Corp");
        
        // Act
        Company result = companyService.updateCompany(testCompany.getId(), updateRequest);
        
        // Assert
        assertNotNull(result);
        
        verify(companyRepository).findById(testCompany.getId());
        verify(companyRepository).save(any(Company.class));
    }
    
    @Test
    public void testVerifyCompany() {
        // Arrange
        when(companyRepository.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);
        
        // Act
        Company result = companyService.verifyCompany(testCompany.getId(), 1L);
        
        // Assert
        assertNotNull(result);
        
        verify(companyRepository).findById(testCompany.getId());
        verify(companyRepository).save(any(Company.class));
    }
    
    @Test
    public void testDeleteCompany() {
        // Arrange
        when(companyRepository.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
        
        // Act
        companyService.deleteCompany(testCompany.getId());
        
        // Assert
        verify(companyRepository).findById(testCompany.getId());
        verify(companyRepository).delete(testCompany);
    }
    
    @Test
    public void testIsUserAdminOfCompany() {
        // Arrange
        when(companyAdminRepository.isUserAdminOfCompany(1L, testCompany.getId())).thenReturn(true);
        
        // Act
        Boolean result = companyService.isUserAdminOfCompany(1L, testCompany.getId());
        
        // Assert
        assertTrue(result);
        
        verify(companyAdminRepository).isUserAdminOfCompany(1L, testCompany.getId());
    }
}
