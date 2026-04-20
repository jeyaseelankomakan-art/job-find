package com.jobmatch.api.controller;

import com.jobmatch.api.model.dto.UserSkillRequest;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.model.entity.UserSkill;
import com.jobmatch.api.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * User Controller
 * Provides REST endpoints for user profile operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Get current user profile
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        log.info("GET /api/v1/users/me - Fetch current user profile");
        
        try {
            User user = userService.getUserByEmail(principal.getName());
            return ResponseEntity.ok(new ApiResponse<>(true, "User profile fetched", user));
        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Get user by ID
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        log.info("GET /api/v1/users/{} - Fetch user by id", id);
        
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User fetched successfully", user));
        } catch (Exception e) {
            log.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Update user profile
     * PUT /api/v1/users/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long id,
            @RequestBody User userDetails,
            Principal principal) {
        log.info("PUT /api/v1/users/{} - Update user profile", id);
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            // Users can only update their own profile
            if (!currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "You can only update your own profile", null));
            }
            
            User updatedUser = userService.updateUserProfile(id, userDetails);
            return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", updatedUser));
        } catch (Exception e) {
            log.error("Error updating user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Get user skills
     * GET /api/v1/users/{id}/skills
     */
    @GetMapping("/{id}/skills")
    public ResponseEntity<?> getUserSkills(@PathVariable Long id) {
        log.info("GET /api/v1/users/{}/skills - Fetch user skills", id);
        
        try {
            List<UserSkill> skills = userService.getUserSkills(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User skills fetched", skills));
        } catch (Exception e) {
            log.error("Error fetching user skills: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Add skill to user
     * POST /api/v1/users/{id}/skills
     */
    @PostMapping("/{id}/skills")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<?> addUserSkill(
            @PathVariable Long id,
            @RequestBody UserSkillRequest skillRequest,
            Principal principal) {
        log.info("POST /api/v1/users/{}/skills - Add skill to user", id);
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            // Users can only add skills to their own profile
            if (!currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "You can only add skills to your own profile", null));
            }
            
            UserSkill userSkill = userService.addUserSkill(id, skillRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Skill added successfully", userSkill));
        } catch (Exception e) {
            log.error("Error adding user skill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Update user skill
     * PUT /api/v1/users/{userId}/skills/{skillId}
     */
    @PutMapping("/{userId}/skills/{skillId}")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<?> updateUserSkill(
            @PathVariable Long userId,
            @PathVariable Long skillId,
            @RequestBody UserSkillRequest skillRequest,
            Principal principal) {
        log.info("PUT /api/v1/users/{}/skills/{} - Update user skill", userId, skillId);
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            // Users can only update skills on their own profile
            if (!currentUser.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "You can only update skills on your own profile", null));
            }
            
            UserSkill userSkill = userService.updateUserSkill(userId, skillId, skillRequest);
            return ResponseEntity.ok(new ApiResponse<>(true, "Skill updated successfully", userSkill));
        } catch (Exception e) {
            log.error("Error updating user skill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Remove user skill
     * DELETE /api/v1/users/{userId}/skills/{skillId}
     */
    @DeleteMapping("/{userId}/skills/{skillId}")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<?> removeUserSkill(
            @PathVariable Long userId,
            @PathVariable Long skillId,
            Principal principal) {
        log.info("DELETE /api/v1/users/{}/skills/{} - Remove user skill", userId, skillId);
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            // Users can only remove skills from their own profile
            if (!currentUser.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "You can only remove skills from your own profile", null));
            }
            
            userService.removeUserSkill(userId, skillId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Skill removed successfully", null));
        } catch (Exception e) {
            log.error("Error removing user skill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Deactivate user account
     * POST /api/v1/users/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<?> deactivateUser(
            @PathVariable Long id,
            Principal principal) {
        log.info("POST /api/v1/users/{}/deactivate - Deactivate user account", id);
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            // Users can only deactivate their own account
            if (!currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "You can only deactivate your own account", null));
            }
            
            userService.deactivateUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Account deactivated successfully", null));
        } catch (Exception e) {
            log.error("Error deactivating account: {}", e.getMessage());
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
