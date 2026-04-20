package com.jobmatch.api.service;

import com.jobmatch.api.exception.ResourceNotFoundException;
import com.jobmatch.api.model.dto.UserSkillRequest;
import com.jobmatch.api.model.entity.Skill;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.model.entity.UserSkill;
import com.jobmatch.api.repository.UserRepository;
import com.jobmatch.api.repository.UserSkillRepository;
import com.jobmatch.api.repository.SkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User Service
 * Handles business logic for user profiles and skills
 */
@Slf4j
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserSkillRepository userSkillRepository;
    
    @Autowired
    private SkillRepository skillRepository;
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        log.debug("Fetching user with id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
    
    /**
     * Update user profile
     */
    @Transactional
    public User updateUserProfile(Long userId, User userDetails) {
        log.info("Updating user profile with id: {}", userId);
        
        User user = getUserById(userId);
        user.setFullName(userDetails.getFullName());
        user.setPhone(userDetails.getPhone());
        
        if (userDetails.getYearsOfExperience() != null) {
            user.setYearsOfExperience(userDetails.getYearsOfExperience());
        }
        if (userDetails.getHighestEducation() != null) {
            user.setHighestEducation(userDetails.getHighestEducation());
        }
        if (userDetails.getPreferredJobTitle() != null) {
            user.setPreferredJobTitle(userDetails.getPreferredJobTitle());
        }
        if (userDetails.getSalaryExpectation() != null) {
            user.setSalaryExpectation(userDetails.getSalaryExpectation());
        }
        if (userDetails.getLocation() != null) {
            user.setLocation(userDetails.getLocation());
        }
        if (userDetails.getCvUrl() != null) {
            user.setCvUrl(userDetails.getCvUrl());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Add skill to user
     */
    @Transactional
    public UserSkill addUserSkill(Long userId, UserSkillRequest skillRequest) {
        log.info("Adding skill to user with id: {}", userId);
        
        User user = getUserById(userId);
        Skill skill = skillRepository.findById(skillRequest.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillRequest.getSkillId()));
        
        // Check if user already has this skill
        Optional<UserSkill> existingSkill = userSkillRepository.findByUserIdAndSkillId(userId, skill.getId());
        if (existingSkill.isPresent()) {
            // Update existing skill
            return updateUserSkill(userId, existingSkill.get().getId(), skillRequest);
        }
        
        UserSkill userSkill = UserSkill.builder()
                .userId(userId)
                .skillId(skill.getId())
                .proficiencyLevel(UserSkill.ProficiencyLevel.valueOf(skillRequest.getProficiencyLevel()))
                .yearsOfExperience(skillRequest.getYearsOfExperience() != null ? skillRequest.getYearsOfExperience() : 0)
                .details(skillRequest.getDetails())
                .build();
        
        return userSkillRepository.save(userSkill);
    }
    
    /**
     * Update user skill
     */
    @Transactional
    public UserSkill updateUserSkill(Long userId, Long userSkillId, UserSkillRequest skillRequest) {
        log.info("Updating user skill with id: {} for user: {}", userSkillId, userId);
        
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found with id: " + userSkillId));
        
        if (!userSkill.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("User skill does not belong to user");
        }
        
        userSkill.setProficiencyLevel(UserSkill.ProficiencyLevel.valueOf(skillRequest.getProficiencyLevel()));
        userSkill.setYearsOfExperience(skillRequest.getYearsOfExperience());
        userSkill.setDetails(skillRequest.getDetails());
        
        return userSkillRepository.save(userSkill);
    }
    
    /**
     * Remove user skill
     */
    @Transactional
    public void removeUserSkill(Long userId, Long skillId) {
        log.info("Removing skill with id: {} from user: {}", skillId, userId);
        
        // Verify user exists
        getUserById(userId);
        
        // Verify skill exists
        skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
        
        userSkillRepository.deleteByUserIdAndSkillId(userId, skillId);
    }
    
    /**
     * Get all user skills
     */
    @Transactional(readOnly = true)
    public List<UserSkill> getUserSkills(Long userId) {
        log.debug("Fetching all skills for user: {}", userId);
        
        // Verify user exists
        getUserById(userId);
        
        return userSkillRepository.findByUserId(userId);
    }
    
    /**
     * Get user skill count
     */
    @Transactional(readOnly = true)
    public Integer getUserSkillCount(Long userId) {
        log.debug("Counting skills for user: {}", userId);
        
        // Verify user exists
        getUserById(userId);
        
        return userSkillRepository.countByUserId(userId);
    }
    
    /**
     * Check if user has a specific skill
     */
    @Transactional(readOnly = true)
    public Boolean userHasSkill(Long userId, Long skillId) {
        log.debug("Checking if user {} has skill {}", userId, skillId);
        return userSkillRepository.hasSkill(userId, skillId);
    }
    
    /**
     * Deactivate user account
     */
    @Transactional
    public void deactivateUser(Long userId) {
        log.info("Deactivating user with id: {}", userId);
        
        User user = getUserById(userId);
        user.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(user);
    }
    
    /**
     * Activate user account
     */
    @Transactional
    public void activateUser(Long userId) {
        log.info("Activating user with id: {}", userId);
        
        User user = getUserById(userId);
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
