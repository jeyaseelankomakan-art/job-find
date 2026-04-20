package com.jobmatch.api.service;

import com.jobmatch.api.exception.ResourceNotFoundException;
import com.jobmatch.api.model.dto.UserSkillRequest;
import com.jobmatch.api.model.entity.Skill;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.model.entity.UserSkill;
import com.jobmatch.api.repository.SkillRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserSkillRepository userSkillRepository;
    
    @Mock
    private SkillRepository skillRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    private Skill testSkill;
    private UserSkill testUserSkill;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role(User.UserRole.JOB_SEEKER)
                .status(User.UserStatus.ACTIVE)
                .yearsOfExperience(5)
                .build();
        
        testSkill = Skill.builder()
                .id(1L)
                .name("Java")
                .category("Programming")
                .build();
        
        testUserSkill = UserSkill.builder()
                .id(1L)
                .userId(testUser.getId())
                .skillId(testSkill.getId())
                .proficiencyLevel(UserSkill.ProficiencyLevel.ADVANCED)
                .yearsOfExperience(5)
                .skill(testSkill)
                .build();
    }
    
    @Test
    public void testGetUserByIdSuccess() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        // Act
        User result = userService.getUserById(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());
        
        verify(userRepository).findById(testUser.getId());
    }
    
    @Test
    public void testGetUserByIdNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
        
        verify(userRepository).findById(999L);
    }
    
    @Test
    public void testGetUserByEmailSuccess() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        
        // Act
        User result = userService.getUserByEmail(testUser.getEmail());
        
        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        
        verify(userRepository).findByEmail(testUser.getEmail());
    }
    
    @Test
    public void testUpdateUserProfile() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        User updateDetails = new User();
        updateDetails.setFullName("Updated Name");
        updateDetails.setPhone("9876543210");
        
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        User result = userService.updateUserProfile(testUser.getId(), updateDetails);
        
        // Assert
        assertNotNull(result);
        
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    public void testAddUserSkillSuccess() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(skillRepository.findById(testSkill.getId())).thenReturn(Optional.of(testSkill));
        when(userSkillRepository.findByUserIdAndSkillId(testUser.getId(), testSkill.getId()))
                .thenReturn(Optional.empty());
        when(userSkillRepository.save(any(UserSkill.class))).thenReturn(testUserSkill);
        
        UserSkillRequest request = new UserSkillRequest();
        request.setSkillId(testSkill.getId());
        request.setProficiencyLevel("ADVANCED");
        request.setYearsOfExperience(5);
        
        // Act
        UserSkill result = userService.addUserSkill(testUser.getId(), request);
        
        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testSkill.getId(), result.getSkillId());
        
        verify(userRepository).findById(testUser.getId());
        verify(skillRepository).findById(testSkill.getId());
        verify(userSkillRepository).save(any(UserSkill.class));
    }
    
    @Test
    public void testGetUserSkills() {
        // Arrange
        List<UserSkill> skills = new ArrayList<>();
        skills.add(testUserSkill);
        
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userSkillRepository.findByUserId(testUser.getId())).thenReturn(skills);
        
        // Act
        List<UserSkill> result = userService.getUserSkills(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserSkill.getId(), result.get(0).getId());
        
        verify(userRepository).findById(testUser.getId());
        verify(userSkillRepository).findByUserId(testUser.getId());
    }
    
    @Test
    public void testRemoveUserSkill() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(skillRepository.findById(testSkill.getId())).thenReturn(Optional.of(testSkill));
        
        // Act
        userService.removeUserSkill(testUser.getId(), testSkill.getId());
        
        // Assert
        verify(userRepository).findById(testUser.getId());
        verify(skillRepository).findById(testSkill.getId());
        verify(userSkillRepository).deleteByUserIdAndSkillId(testUser.getId(), testSkill.getId());
    }
    
    @Test
    public void testUserHasSkill() {
        // Arrange
        when(userSkillRepository.hasSkill(testUser.getId(), testSkill.getId())).thenReturn(true);
        
        // Act
        Boolean result = userService.userHasSkill(testUser.getId(), testSkill.getId());
        
        // Assert
        assertTrue(result);
        
        verify(userSkillRepository).hasSkill(testUser.getId(), testSkill.getId());
    }
    
    @Test
    public void testDeactivateUser() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        userService.deactivateUser(testUser.getId());
        
        // Assert
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(any(User.class));
    }
}
