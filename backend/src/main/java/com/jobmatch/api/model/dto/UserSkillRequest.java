package com.jobmatch.api.model.dto;

/**
 * DTO for creating/updating user skills
 */
public class UserSkillRequest {
    
    private Long skillId;
    private String skillName;
    private String proficiencyLevel; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    private Integer yearsOfExperience;
    private String details;
    
    // Constructors
    public UserSkillRequest() {}
    
    public UserSkillRequest(Long skillId, String proficiencyLevel, Integer yearsOfExperience) {
        this.skillId = skillId;
        this.proficiencyLevel = proficiencyLevel;
        this.yearsOfExperience = yearsOfExperience;
    }
    
    // Getters and Setters
    public Long getSkillId() {
        return skillId;
    }
    
    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }
    
    public String getSkillName() {
        return skillName;
    }
    
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
    
    public String getProficiencyLevel() {
        return proficiencyLevel;
    }
    
    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }
    
    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }
    
    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
}
