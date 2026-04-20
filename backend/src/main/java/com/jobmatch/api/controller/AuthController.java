package com.jobmatch.api.controller;

import com.jobmatch.api.model.dto.AuthResponse;
import com.jobmatch.api.model.dto.LoginRequest;
import com.jobmatch.api.model.dto.RegisterRequest;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Authentication Controller
 * Provides REST endpoints for authentication operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * Register a new user
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email: {}", request.getEmail());
        
        try {
            User user = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "User registered successfully", user));
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    /**
     * Login user
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", response));
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid email or password", null));
        }
    }
    
    /**
     * Refresh access token
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");
        
        try {
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed successfully", response));
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid refresh token", null));
        }
    }
    
    /**
     * Logout user (client-side removes token)
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('COMPANY_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<?> logout() {
        log.info("Logout request");
        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out successfully", null));
    }
    
    /**
     * Validate token
     * GET /api/v1/auth/validate
     */
    @GetMapping("/validate")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('COMPANY_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<?> validateToken() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Token is valid", null));
    }
    
    /**
     * Request password reset
     * POST /api/v1/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        
        try {
            // TODO: Implement email sending for password reset
            return ResponseEntity.ok(new ApiResponse<>(true, "If email exists, reset link will be sent", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to process request", null));
        }
    }
    
    /**
     * Reset password
     * POST /api/v1/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Reset password request");
        
        try {
            // TODO: Implement password reset logic
            return ResponseEntity.ok(new ApiResponse<>(true, "Password reset successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to reset password", null));
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
        
        // Getters
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
    
    /**
     * Refresh token request DTO
     */
    public static class RefreshTokenRequest {
        private String refreshToken;
        
        public RefreshTokenRequest() {}
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
    
    /**
     * Forgot password request DTO
     */
    public static class ForgotPasswordRequest {
        private String email;
        
        public ForgotPasswordRequest() {}
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    /**
     * Reset password request DTO
     */
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
        
        public ResetPasswordRequest() {}
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public String getNewPassword() {
            return newPassword;
        }
        
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
