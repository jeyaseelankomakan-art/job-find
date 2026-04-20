package com.jobmatch.api.service;

import com.jobmatch.api.exception.ResourceNotFoundException;
import com.jobmatch.api.model.entity.Skill;
import com.jobmatch.api.repository.SkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Skill Service
 * Handles business logic for skills
 */
@Slf4j
@Service
public class SkillService {
    
    @Autowired
    private SkillRepository skillRepository;
    
    /**
     * Get all skills
     */
    @Transactional(readOnly = true)
    public List<Skill> getAllSkills() {
        log.debug("Fetching all skills");
        return skillRepository.findAll();
    }
    
    /**
     * Get skill by ID
     */
    @Transactional(readOnly = true)
    public Skill getSkillById(Long skillId) {
        log.debug("Fetching skill with id: {}", skillId);
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
    }
    
    /**
     * Search skills by name
     */
    @Transactional(readOnly = true)
    public List<Skill> searchSkills(String name) {
        log.debug("Searching skills with name: {}", name);
        return skillRepository.searchByName(name);
    }
    
    /**
     * Get skills by category
     */
    @Transactional(readOnly = true)
    public List<Skill> getSkillsByCategory(String category) {
        log.debug("Fetching skills by category: {}", category);
        return skillRepository.findByCategory(category);
    }
    
    /**
     * Get top popular skills
     */
    @Transactional(readOnly = true)
    public List<Skill> getTopPopularSkills(int limit) {
        log.debug("Fetching top {} popular skills", limit);
        return skillRepository.getTopPopularSkills(limit);
    }
    
    /**
     * Create new skill
     */
    @Transactional
    public Skill createSkill(Skill skill) {
        log.info("Creating new skill: {}", skill.getName());
        return skillRepository.save(skill);
    }
    
    /**
     * Update skill
     */
    @Transactional
    public Skill updateSkill(Long skillId, Skill skillDetails) {
        log.info("Updating skill with id: {}", skillId);
        
        Skill skill = getSkillById(skillId);
        skill.setName(skillDetails.getName());
        skill.setDescription(skillDetails.getDescription());
        skill.setCategory(skillDetails.getCategory());
        skill.setPopularity(skillDetails.getPopularity());
        
        return skillRepository.save(skill);
    }
    
    /**
     * Delete skill
     */
    @Transactional
    public void deleteSkill(Long skillId) {
        log.info("Deleting skill with id: {}", skillId);
        
        Skill skill = getSkillById(skillId);
        skillRepository.delete(skill);
    }
    
    /**
     * Get or create skill by name
     */
    @Transactional
    public Skill getOrCreateSkill(String skillName, String category) {
        log.debug("Getting or creating skill: {}", skillName);
        
        return skillRepository.findByName(skillName)
                .orElseGet(() -> {
                    Skill newSkill = Skill.builder()
                            .name(skillName)
                            .category(category)
                            .popularity(0)
                            .build();
                    return skillRepository.save(newSkill);
                });
    }
}
