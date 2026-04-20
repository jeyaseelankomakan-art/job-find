package com.jobmatch.api.service;

import com.jobmatch.api.exception.ResourceNotFoundException;
import com.jobmatch.api.model.entity.Skill;
import com.jobmatch.api.repository.SkillRepository;
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
 * Unit tests for SkillService
 */
@ExtendWith(MockitoExtension.class)
public class SkillServiceTest {
    
    @Mock
    private SkillRepository skillRepository;
    
    @InjectMocks
    private SkillService skillService;
    
    private Skill testSkill;
    
    @BeforeEach
    public void setUp() {
        testSkill = Skill.builder()
                .id(1L)
                .name("Java")
                .description("Java programming language")
                .category("Programming")
                .popularity(100)
                .build();
    }
    
    @Test
    public void testGetAllSkills() {
        // Arrange
        List<Skill> skills = new ArrayList<>();
        skills.add(testSkill);
        
        when(skillRepository.findAll()).thenReturn(skills);
        
        // Act
        List<Skill> result = skillService.getAllSkills();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java", result.get(0).getName());
        
        verify(skillRepository).findAll();
    }
    
    @Test
    public void testGetSkillByIdSuccess() {
        // Arrange
        when(skillRepository.findById(testSkill.getId())).thenReturn(Optional.of(testSkill));
        
        // Act
        Skill result = skillService.getSkillById(testSkill.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals("Java", result.getName());
        assertEquals("Programming", result.getCategory());
        
        verify(skillRepository).findById(testSkill.getId());
    }
    
    @Test
    public void testGetSkillByIdNotFound() {
        // Arrange
        when(skillRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            skillService.getSkillById(999L);
        });
        
        verify(skillRepository).findById(999L);
    }
    
    @Test
    public void testSearchSkills() {
        // Arrange
        List<Skill> skills = new ArrayList<>();
        skills.add(testSkill);
        
        when(skillRepository.searchByName("Java")).thenReturn(skills);
        
        // Act
        List<Skill> result = skillService.searchSkills("Java");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java", result.get(0).getName());
        
        verify(skillRepository).searchByName("Java");
    }
    
    @Test
    public void testGetSkillsByCategory() {
        // Arrange
        List<Skill> skills = new ArrayList<>();
        skills.add(testSkill);
        
        when(skillRepository.findByCategory("Programming")).thenReturn(skills);
        
        // Act
        List<Skill> result = skillService.getSkillsByCategory("Programming");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Programming", result.get(0).getCategory());
        
        verify(skillRepository).findByCategory("Programming");
    }
    
    @Test
    public void testGetTopPopularSkills() {
        // Arrange
        List<Skill> skills = new ArrayList<>();
        skills.add(testSkill);
        
        when(skillRepository.getTopPopularSkills(10)).thenReturn(skills);
        
        // Act
        List<Skill> result = skillService.getTopPopularSkills(10);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(skillRepository).getTopPopularSkills(10);
    }
    
    @Test
    public void testCreateSkill() {
        // Arrange
        when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);
        
        // Act
        Skill result = skillService.createSkill(testSkill);
        
        // Assert
        assertNotNull(result);
        assertEquals("Java", result.getName());
        
        verify(skillRepository).save(any(Skill.class));
    }
    
    @Test
    public void testUpdateSkill() {
        // Arrange
        when(skillRepository.findById(testSkill.getId())).thenReturn(Optional.of(testSkill));
        when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);
        
        Skill updateDetails = new Skill();
        updateDetails.setName("Updated Java");
        updateDetails.setPopularity(150);
        
        // Act
        Skill result = skillService.updateSkill(testSkill.getId(), updateDetails);
        
        // Assert
        assertNotNull(result);
        
        verify(skillRepository).findById(testSkill.getId());
        verify(skillRepository).save(any(Skill.class));
    }
    
    @Test
    public void testDeleteSkill() {
        // Arrange
        when(skillRepository.findById(testSkill.getId())).thenReturn(Optional.of(testSkill));
        
        // Act
        skillService.deleteSkill(testSkill.getId());
        
        // Assert
        verify(skillRepository).findById(testSkill.getId());
        verify(skillRepository).delete(testSkill);
    }
    
    @Test
    public void testGetOrCreateSkillExists() {
        // Arrange
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(testSkill));
        
        // Act
        Skill result = skillService.getOrCreateSkill("Java", "Programming");
        
        // Assert
        assertNotNull(result);
        assertEquals("Java", result.getName());
        
        verify(skillRepository).findByName("Java");
        verify(skillRepository, never()).save(any());
    }
    
    @Test
    public void testGetOrCreateSkillNew() {
        // Arrange
        when(skillRepository.findByName("Python")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenReturn(
                Skill.builder().id(2L).name("Python").category("Programming").build()
        );
        
        // Act
        Skill result = skillService.getOrCreateSkill("Python", "Programming");
        
        // Assert
        assertNotNull(result);
        assertEquals("Python", result.getName());
        
        verify(skillRepository).findByName("Python");
        verify(skillRepository).save(any(Skill.class));
    }
}
