package com.jobmatch.api.service;

import com.jobmatch.api.exception.ResourceNotFoundException;
import com.jobmatch.api.model.dto.JobRequest;
import com.jobmatch.api.model.dto.JobSearchCriteria;
import com.jobmatch.api.model.entity.Job;
import com.jobmatch.api.repository.JobRepository;
import com.jobmatch.api.repository.JobSkillRepository;
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
 * Unit tests for JobService
 */
@ExtendWith(MockitoExtension.class)
public class JobServiceTest {
    
    @Mock
    private JobRepository jobRepository;
    
    @Mock
    private JobSkillRepository jobSkillRepository;
    
    @InjectMocks
    private JobService jobService;
    
    private Job testJob;
    private JobRequest testRequest;
    
    @BeforeEach
    public void setUp() {
        testJob = Job.builder()
                .id(1L)
                .companyId(1L)
                .title("Senior Java Developer")
                .description("We are looking for a senior Java developer")
                .jobType("FULL_TIME")
                .location("San Francisco, CA")
                .remote(false)
                .salaryMin(120000L)
                .salaryMax(160000L)
                .experienceLevel("SENIOR")
                .status(Job.JobStatus.PUBLISHED)
                .build();
        
        testRequest = JobRequest.builder()
                .title("Senior Java Developer")
                .description("We are looking for a senior Java developer")
                .jobType("FULL_TIME")
                .location("San Francisco, CA")
                .remote(false)
                .salaryMin(120000L)
                .salaryMax(160000L)
                .experienceLevel("SENIOR")
                .build();
    }
    
    @Test
    public void testGetJobByIdSuccess() {
        // Arrange
        when(jobRepository.findById(testJob.getId())).thenReturn(Optional.of(testJob));
        
        // Act
        Job result = jobService.getJobById(testJob.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(testJob.getId(), result.getId());
        assertEquals("Senior Java Developer", result.getTitle());
        
        verify(jobRepository).findById(testJob.getId());
    }
    
    @Test
    public void testGetJobByIdNotFound() {
        // Arrange
        when(jobRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            jobService.getJobById(999L);
        });
        
        verify(jobRepository).findById(999L);
    }
    
    @Test
    public void testGetAllPublishedJobs() {
        // Arrange
        List<Job> jobs = new ArrayList<>();
        jobs.add(testJob);
        Page<Job> page = new PageImpl<>(jobs);
        
        when(jobRepository.findPublished(any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<Job> result = jobService.getAllPublishedJobs(0, 20);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        verify(jobRepository).findPublished(any(Pageable.class));
    }
    
    @Test
    public void testGetJobsByCompany() {
        // Arrange
        List<Job> jobs = new ArrayList<>();
        jobs.add(testJob);
        Page<Job> page = new PageImpl<>(jobs);
        
        when(jobRepository.findByCompanyId(eq(testJob.getCompanyId()), any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<Job> result = jobService.getJobsByCompany(testJob.getCompanyId(), 0, 20);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        verify(jobRepository).findByCompanyId(eq(testJob.getCompanyId()), any(Pageable.class));
    }
    
    @Test
    public void testSearchJobs() {
        // Arrange
        List<Job> jobs = new ArrayList<>();
        jobs.add(testJob);
        Page<Job> page = new PageImpl<>(jobs);
        
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .title("Java")
                .location("San Francisco")
                .salaryMin(100000L)
                .experienceLevel("SENIOR")
                .page(0)
                .pageSize(20)
                .build();
        
        when(jobRepository.searchJobs(any(), any(), nullable(Long.class), nullable(Long.class),
            any(), any(), nullable(Long.class), any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<Job> result = jobService.searchJobs(criteria);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        verify(jobRepository).searchJobs(any(), any(), nullable(Long.class), nullable(Long.class),
            any(), any(), nullable(Long.class), any(Pageable.class));
    }
    
    @Test
    public void testSearchByTitle() {
        // Arrange
        List<Job> jobs = new ArrayList<>();
        jobs.add(testJob);
        Page<Job> page = new PageImpl<>(jobs);
        
        when(jobRepository.searchByTitle(eq("Java"), any(Pageable.class))).thenReturn(page);
        
        // Act
        Page<Job> result = jobService.searchByTitle("Java", 0, 20);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        verify(jobRepository).searchByTitle(eq("Java"), any(Pageable.class));
    }
    
    @Test
    public void testCreateJob() {
        // Arrange
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Job result = jobService.createJob(testJob.getCompanyId(), testRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("Senior Java Developer", result.getTitle());
        assertEquals(Job.JobStatus.DRAFT, result.getStatus());
        
        verify(jobRepository).save(any(Job.class));
    }
    
    @Test
    public void testPublishJob() {
        // Arrange
        testJob.setStatus(Job.JobStatus.DRAFT);
        when(jobRepository.findById(testJob.getId())).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        
        // Act
        Job result = jobService.publishJob(testJob.getId());
        
        // Assert
        assertNotNull(result);
        
        verify(jobRepository).findById(testJob.getId());
        verify(jobRepository).save(any(Job.class));
    }
    
    @Test
    public void testUpdateJob() {
        // Arrange
        when(jobRepository.findById(testJob.getId())).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        
        JobRequest updateRequest = new JobRequest();
        updateRequest.setTitle("Updated Senior Java Developer");
        
        // Act
        Job result = jobService.updateJob(testJob.getId(), updateRequest);
        
        // Assert
        assertNotNull(result);
        
        verify(jobRepository).findById(testJob.getId());
        verify(jobRepository).save(any(Job.class));
    }
    
    @Test
    public void testCloseJob() {
        // Arrange
        when(jobRepository.findById(testJob.getId())).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        
        // Act
        Job result = jobService.closeJob(testJob.getId());
        
        // Assert
        assertNotNull(result);
        
        verify(jobRepository).findById(testJob.getId());
        verify(jobRepository).save(any(Job.class));
    }
    
    @Test
    public void testDeleteJob() {
        // Arrange
        when(jobRepository.findById(testJob.getId())).thenReturn(Optional.of(testJob));
        
        // Act
        jobService.deleteJob(testJob.getId());
        
        // Assert
        verify(jobRepository).findById(testJob.getId());
        verify(jobRepository).delete(testJob);
    }
}
