package com.jobmatch.api.service;

import com.jobmatch.api.model.entity.Job;
import com.jobmatch.api.model.entity.JobSkill;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.model.entity.UserSkill;
import com.jobmatch.api.repository.JobRepository;
import com.jobmatch.api.repository.JobSkillRepository;
import com.jobmatch.api.repository.UserRepository;
import com.jobmatch.api.repository.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MatchingService
 */
@ExtendWith(MockitoExtension.class)
public class MatchingServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JobRepository jobRepository;
    
    @Mock
    private UserSkillRepository userSkillRepository;
    
    @Mock
    private JobSkillRepository jobSkillRepository;
    
    @InjectMocks
    private MatchingService matchingService;
    
    private User testUser;
    private Job testJob;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .yearsOfExperience(5)
                .location("San Francisco, CA")
            .salaryExpectation(130000L)
            .phone("9876543210")
                .build();
        
        testJob = Job.builder()
                .id(1L)
                .companyId(1L)
                .title("Senior Java Developer")
                .location("San Francisco, CA")
                .remote(false)
                .salaryMin(120000L)
                .salaryMax(160000L)
                .experienceLevel("SENIOR")
                .build();
    }
    
    @Test
    public void testCalculateMatchScorePerfectMatch() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jobRepository.findById(testJob.getId())).thenReturn(Optional.of(testJob));
        when(jobSkillRepository.findMandatorySkillsByJobId(testJob.getId())).thenReturn(new ArrayList<>());
        when(jobSkillRepository.findByJobId(testJob.getId())).thenReturn(new ArrayList<>());
        
        // Act
        Double score = matchingService.calculateMatchScore(testUser.getId(), testJob.getId());
        
        // Assert
        assertNotNull(score);
        assertTrue(score >= 0 && score <= 100);
        
        verify(userRepository).findById(testUser.getId());
        verify(jobRepository).findById(testJob.getId());
    }
    
    @Test
    public void testCalculateMatchScoreMissingMandatorySkills() {
        // Arrange
        List<JobSkill> mandatorySkills = new ArrayList<>();
        JobSkill skill = new JobSkill();
        skill.setId(1L);
        skill.setSkillId(1L);
        skill.setMandatory(true);
        mandatorySkills.add(skill);
        
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jobRepository.findById(testJob.getId())).thenReturn(Optional.of(testJob));
        when(jobSkillRepository.findMandatorySkillsByJobId(testJob.getId())).thenReturn(mandatorySkills);
        when(userSkillRepository.hasSkill(testUser.getId(), 1L)).thenReturn(false);
        
        // Act
        Double score = matchingService.calculateMatchScore(testUser.getId(), testJob.getId());
        
        // Assert
        assertEquals(0.0, score);
        
        verify(userRepository).findById(testUser.getId());
        verify(jobRepository).findById(testJob.getId());
    }
    
    @Test
    public void testCalculateMatchScoreWithSkills() {
        // Arrange
        List<JobSkill> mandatorySkills = new ArrayList<>();
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jobRepository.findById(testJob.getId())).thenReturn(Optional.of(testJob));
        when(jobSkillRepository.findMandatorySkillsByJobId(testJob.getId())).thenReturn(mandatorySkills);
        
        List<JobSkill> requiredSkills = new ArrayList<>();
        JobSkill skill = JobSkill.builder()
                .id(1L)
                .skillId(1L)
            .requiredLevel(UserSkill.ProficiencyLevel.ADVANCED)
                .mandatory(true)
                .build();
        requiredSkills.add(skill);
        
        UserSkill userSkill = UserSkill.builder()
                .userId(testUser.getId())
                .skillId(1L)
                .proficiencyLevel(UserSkill.ProficiencyLevel.EXPERT)
                .build();
        
        when(jobSkillRepository.findByJobId(testJob.getId())).thenReturn(requiredSkills);
        when(userSkillRepository.findByUserIdAndSkillId(testUser.getId(), 1L))
                .thenReturn(Optional.of(userSkill));
        
        // Act
        Double score = matchingService.calculateMatchScore(testUser.getId(), testJob.getId());
        
        // Assert
        assertNotNull(score);
        assertTrue(score > 0);
        
        verify(userRepository).findById(testUser.getId());
        verify(jobRepository).findById(testJob.getId());
    }
}
