package com.jobmatch.api.service;

import com.jobmatch.api.exception.DuplicateResourceException;
import com.jobmatch.api.model.dto.AuthResponse;
import com.jobmatch.api.model.dto.LoginRequest;
import com.jobmatch.api.model.dto.RegisterRequest;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.model.entity.User.UserRole;
import com.jobmatch.api.model.entity.User.UserStatus;
import com.jobmatch.api.repository.UserRepository;
import com.jobmatch.api.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication Service
 * Handles user registration, login, and token generation
 */
@Slf4j
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * Register a new user
     */
    @Transactional
    public User register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered: " + request.getEmail());
        }
        
        // Encode password
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(UserRole.valueOf(request.getRole()))
                .status(UserStatus.PENDING_VERIFICATION)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());
        
        return savedUser;
    }
    
    /**
     * Authenticate user and generate tokens
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        
        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(user.getEmail(), user.getRole().toString());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
        Long expiresIn = tokenProvider.getTokenExpirationSeconds();
        
        log.info("User logged in successfully: {}", request.getEmail());
        
        // Build response
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().toString(),
                user.getStatus().toString()
        );
        
        return new AuthResponse(accessToken, refreshToken, expiresIn, userDto);
    }
    
    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token is invalid");
        }
        
        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate new access token
        String newAccessToken = tokenProvider.generateAccessToken(user.getEmail(), user.getRole().toString());
        Long expiresIn = tokenProvider.getTokenExpirationSeconds();
        
        log.debug("Token refreshed for user: {}", email);
        
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().toString(),
                user.getStatus().toString()
        );
        
        return new AuthResponse(newAccessToken, refreshToken, expiresIn, userDto);
    }
    
    /**
     * Validate token
     */
    public Boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }
}
