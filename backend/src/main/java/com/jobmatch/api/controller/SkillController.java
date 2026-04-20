package com.jobmatch.api.controller;

import com.jobmatch.api.model.dto.ApiResponse;
import com.jobmatch.api.model.entity.Skill;
import com.jobmatch.api.service.SkillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Skill Controller
 * Provides REST endpoints for skill operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/skills")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SkillController {
    
    @Autowired
    private SkillService skillService;
    
    /**
     * Get all skills
     * GET /api/v1/skills
     */
    @GetMapping
    public ResponseEntity<?> getAllSkills() {
        log.info("GET /api/v1/skills - Fetch all skills");
        
        try {
            List<Skill> skills = skillService.getAllSkills();
            return ResponseEntity.ok(new ApiResponse<>(true, "Skills fetched successfully", skills));
        } catch (Exception e) {
            log.error("Error fetching skills: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Get skill by ID
     * GET /api/v1/skills/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSkillById(@PathVariable Long id) {
        log.info("GET /api/v1/skills/{} - Fetch skill by id", id);
        
        try {
            Skill skill = skillService.getSkillById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Skill fetched successfully", skill));
        } catch (Exception e) {
            log.error("Error fetching skill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Search skills
     * GET /api/v1/skills/search?q=java
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchSkills(@RequestParam String q) {
        log.info("GET /api/v1/skills/search - Search skills with query: {}", q);
        
        try {
            List<Skill> skills = skillService.searchSkills(q);
            return ResponseEntity.ok(new ApiResponse<>(true, "Skills found", skills));
        } catch (Exception e) {
            log.error("Error searching skills: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Get skills by category
     * GET /api/v1/skills/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getSkillsByCategory(@PathVariable String category) {
        log.info("GET /api/v1/skills/category/{} - Fetch skills by category", category);
        
        try {
            List<Skill> skills = skillService.getSkillsByCategory(category);
            return ResponseEntity.ok(new ApiResponse<>(true, "Skills fetched successfully", skills));
        } catch (Exception e) {
            log.error("Error fetching skills: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Get top popular skills
     * GET /api/v1/skills/popular?limit=10
     */
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularSkills(
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("GET /api/v1/skills/popular - Fetch top {} popular skills", limit);
        
        try {
            List<Skill> skills = skillService.getTopPopularSkills(limit);
            return ResponseEntity.ok(new ApiResponse<>(true, "Popular skills fetched", skills));
        } catch (Exception e) {
            log.error("Error fetching popular skills: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Create new skill (Admin only)
     * POST /api/v1/skills
     */
    @PostMapping
    public ResponseEntity<?> createSkill(@RequestBody Skill skill) {
        log.info("POST /api/v1/skills - Create new skill: {}", skill.getName());
        
        try {
            Skill createdSkill = skillService.createSkill(skill);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Skill created successfully", createdSkill));
        } catch (Exception e) {
            log.error("Error creating skill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Update skill (Admin only)
     * PUT /api/v1/skills/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSkill(@PathVariable Long id, @RequestBody Skill skillDetails) {
        log.info("PUT /api/v1/skills/{} - Update skill", id);
        
        try {
            Skill updatedSkill = skillService.updateSkill(id, skillDetails);
            return ResponseEntity.ok(new ApiResponse<>(true, "Skill updated successfully", updatedSkill));
        } catch (Exception e) {
            log.error("Error updating skill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Delete skill (Admin only)
     * DELETE /api/v1/skills/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSkill(@PathVariable Long id) {
        log.info("DELETE /api/v1/skills/{} - Delete skill", id);
        
        try {
            skillService.deleteSkill(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Skill deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting skill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Generic API Response wrapper
     */
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        
        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public T getData() {
            return data;
        }
    }
}
