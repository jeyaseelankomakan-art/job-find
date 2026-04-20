package com.jobmatch.api.service;

import com.jobmatch.api.model.entity.*;
import com.jobmatch.api.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Matching Service
 * Business logic for job matching algorithm
 * 
 * Matching Score Formula:
 * - Skill Match: 40% (if mandatory skills missing, score = 0%)
 * - Experience Match: 30%
 * - Location Match: 15%
 * - Salary Match: 10%
 * - Bonus: 5%
 */
@Service
@Transactional
@Slf4j
public class MatchingService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private UserSkillRepository userSkillRepository;
    
    @Autowired
    private JobSkillRepository jobSkillRepository;
    
    private static final double SKILL_WEIGHT = 0.40;
    private static final double EXPERIENCE_WEIGHT = 0.30;
    private static final double LOCATION_WEIGHT = 0.15;
    private static final double SALARY_WEIGHT = 0.10;
    private static final double BONUS_WEIGHT = 0.05;
    
    /**
     * Calculate match score between user and job
     * Returns 0-100 score
     */
    public Double calculateMatchScore(Long userId, Long jobId) {
        log.debug("Calculating match score for user: {}, job: {}", userId, jobId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        // Check mandatory skills first - if missing any, score = 0%
        if (!hasMandatorySkills(userId, jobId)) {
            log.debug("User {} missing mandatory skills for job {}", userId, jobId);
            return 0.0;
        }
        
        // Calculate weighted scores
        double skillScore = calculateSkillMatch(userId, jobId) * SKILL_WEIGHT;
        double experienceScore = calculateExperienceMatch(user, job) * EXPERIENCE_WEIGHT;
        double locationScore = calculateLocationMatch(user, job) * LOCATION_WEIGHT;
        double salaryScore = calculateSalaryMatch(user, job) * SALARY_WEIGHT;
        double bonusScore = calculateBonusMatch(user, job) * BONUS_WEIGHT;
        
        double totalScore = skillScore + experienceScore + locationScore + salaryScore + bonusScore;
        
        log.debug("Match score components - Skills: {}, Experience: {}, Location: {}, Salary: {}, Bonus: {}",
                skillScore, experienceScore, locationScore, salaryScore, bonusScore);
        
        return Math.min(100.0, totalScore);
    }
    
    /**
     * Check if user has all mandatory skills for job
     */
    private Boolean hasMandatorySkills(Long userId, Long jobId) {
        log.debug("Checking mandatory skills for user: {}, job: {}", userId, jobId);
        
        List<JobSkill> mandatorySkills = jobSkillRepository.findMandatorySkillsByJobId(jobId);
        
        for (JobSkill jobSkill : mandatorySkills) {
            boolean hasSkill = userSkillRepository.hasSkill(userId, jobSkill.getSkillId());
            if (!hasSkill) {
                log.debug("User {} missing mandatory skill: {}", userId, jobSkill.getSkillId());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calculate skill match score (0-100)
     * Based on matching required skills and proficiency levels
     */
    private Double calculateSkillMatch(Long userId, Long jobId) {
        List<JobSkill> requiredSkills = jobSkillRepository.findByJobId(jobId);
        
        if (requiredSkills.isEmpty()) {
            return 100.0; // No skills required
        }
        
        int matchedSkills = 0;
        double totalProficiencyScore = 0.0;
        
        for (JobSkill jobSkill : requiredSkills) {
            UserSkill userSkill = userSkillRepository
                    .findByUserIdAndSkillId(userId, jobSkill.getSkillId())
                    .orElse(null);
            
            if (userSkill != null) {
                matchedSkills++;
                
                // Map proficiency levels to scores
                int userLevel = getProficiencyScore(userSkill.getProficiencyLevel());
                int requiredLevel = getProficiencyScore(jobSkill.getRequiredLevel());
                
                // If user skill level >= required, add full points
                if (userLevel >= requiredLevel) {
                    totalProficiencyScore += 100.0;
                } else {
                    // Partial points based on how close user level is
                    totalProficiencyScore += (userLevel / (double) requiredLevel) * 100.0;
                }
            }
        }
        
        return (matchedSkills / (double) requiredSkills.size()) * 
               (totalProficiencyScore / (double) requiredSkills.size());
    }
    
    /**
     * Map proficiency level enum to numeric score
     */
    private int getProficiencyScore(UserSkill.ProficiencyLevel level) {
        switch (level) {
            case BEGINNER:
                return 1;
            case INTERMEDIATE:
                return 2;
            case ADVANCED:
                return 3;
            case EXPERT:
                return 4;
            default:
                return 0;
        }
    }
    
    /**
     * Map required proficiency level to numeric score
     */
    private int getProficiencyScore(String level) {
        if (level == null) return 1;
        
        switch (level.toUpperCase()) {
            case "BEGINNER":
                return 1;
            case "INTERMEDIATE":
                return 2;
            case "ADVANCED":
                return 3;
            case "EXPERT":
                return 4;
            default:
                return 1;
        }
    }
    
    /**
     * Calculate experience match score (0-100)
     * Based on user's years of experience vs job requirement
     */
    private Double calculateExperienceMatch(User user, Job job) {
        if (user.getYearsOfExperience() == null || job.getExperienceLevel() == null) {
            return 50.0; // Neutral if information missing
        }
        
        int userYears = user.getYearsOfExperience();
        int requiredYears = getMinYearsForLevel(job.getExperienceLevel());
        
        if (userYears >= requiredYears) {
            return 100.0;
        } else {
            // Partial credit based on how close user is to requirement
            return (userYears / (double) requiredYears) * 100.0;
        }
    }
    
    /**
     * Map experience level to minimum years required
     */
    private int getMinYearsForLevel(String level) {
        if (level == null) return 0;
        
        switch (level.toUpperCase()) {
            case "ENTRY":
                return 0;
            case "MID":
                return 3;
            case "SENIOR":
                return 7;
            case "LEAD":
                return 10;
            default:
                return 0;
        }
    }
    
    /**
     * Calculate location match score (0-100)
     */
    private Double calculateLocationMatch(User user, Job job) {
        // Perfect match if job is remote
        if (job.getRemote()) {
            return 100.0;
        }
        
        if (user.getLocation() == null || job.getLocation() == null) {
            return 50.0; // Neutral if information missing
        }
        
        // Exact match
        if (user.getLocation().equalsIgnoreCase(job.getLocation())) {
            return 100.0;
        }
        
        // Partial match if same city/region (simple check)
        String userCity = extractCity(user.getLocation());
        String jobCity = extractCity(job.getLocation());
        
        if (userCity != null && userCity.equalsIgnoreCase(jobCity)) {
            return 80.0; // Same city but different specific location
        }
        
        return 30.0; // Different locations
    }
    
    /**
     * Extract city from location string
     */
    private String extractCity(String location) {
        if (location == null) return null;
        
        // Simple extraction - take first part before comma or first word
        String[] parts = location.split(",");
        return parts[0].trim();
    }
    
    /**
     * Calculate salary match score (0-100)
     */
    private Double calculateSalaryMatch(User user, Job job) {
        if (user.getSalaryExpectation() == null || 
            job.getSalaryMin() == null || job.getSalaryMax() == null) {
            return 50.0; // Neutral if information missing
        }
        
        Long userExpected = user.getSalaryExpectation();
        Long jobMin = job.getSalaryMin();
        Long jobMax = job.getSalaryMax();
        
        // Perfect match if user expectation within range
        if (userExpected >= jobMin && userExpected <= jobMax) {
            return 100.0;
        }
        
        // If user expectation is above range, still give partial credit
        if (userExpected > jobMax) {
            double excess = userExpected - jobMax;
            double tolerance = jobMax * 0.2; // 20% tolerance
            
            if (excess <= tolerance) {
                return 80.0 - (excess / tolerance) * 30.0;
            }
            return 50.0;
        }
        
        // If user expectation is below range, high match
        if (userExpected < jobMin) {
            double deficit = jobMin - userExpected;
            double jobRange = jobMax - jobMin;
            
            return 100.0 - (deficit / jobRange) * 50.0;
        }
        
        return 50.0;
    }
    
    /**
     * Calculate bonus match score (0-100)
     * Based on user profile completeness and activity
     */
    private Double calculateBonusMatch(User user, Job job) {
        double bonusScore = 0.0;
        
        // Bonus for profile completeness
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            bonusScore += 20.0;
        }
        if (user.getLocation() != null && !user.getLocation().isEmpty()) {
            bonusScore += 20.0;
        }
        if (user.getYearsOfExperience() != null && user.getYearsOfExperience() > 0) {
            bonusScore += 20.0;
        }
        if (user.getSalaryExpectation() != null) {
            bonusScore += 20.0;
        }
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            bonusScore += 20.0;
        }
        
        return Math.min(100.0, bonusScore);
    }
}
