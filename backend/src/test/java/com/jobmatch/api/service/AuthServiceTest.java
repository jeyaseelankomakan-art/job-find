package com.jobmatch.api.service;

import com.jobmatch.api.exception.DuplicateResourceException;
import com.jobmatch.api.model.dto.LoginRequest;
import com.jobmatch.api.model.dto.RegisterRequest;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.repository.UserRepository;
import com.jobmatch.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider tokenProvider;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @InjectMocks
    private AuthService authService;
    
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    
    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequest(
                "test@example.com",
                "password123",
                "Test User",
                "1234567890",
                "JOB_SEEKER"
        );
        
        loginRequest = new LoginRequest(
                "test@example.com",
                "password123"
        );
        
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .fullName("Test User")
                .role(User.UserRole.JOB_SEEKER)
                .status(User.UserStatus.ACTIVE)
                .build();
    }
    
    @Test
    public void testRegisterSuccess() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        User result = authService.register(registerRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName());
        
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    public void testRegisterDuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);
        
        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(registerRequest);
        });
        
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any());
    }
    
    @Test
    public void testLoginSuccess() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("accessToken");
        when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(tokenProvider.getTokenExpirationSeconds()).thenReturn(86400L);
        
        // Act
        var result = authService.login(loginRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
        assertNotNull(result.getUser());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(tokenProvider).generateAccessToken(anyString(), anyString());
        verify(tokenProvider).generateRefreshToken(anyString());
    }
    
    @Test
    public void testRefreshToken() {
        // Arrange
        String refreshToken = "refreshToken";
        when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(refreshToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("newAccessToken");
        when(tokenProvider.getTokenExpirationSeconds()).thenReturn(86400L);
        
        // Act
        var result = authService.refreshToken(refreshToken);
        
        // Assert
        assertNotNull(result);
        assertEquals("newAccessToken", result.getAccessToken());
        
        verify(tokenProvider).validateToken(refreshToken);
        verify(tokenProvider).getEmailFromToken(refreshToken);
    }
    
    @Test
    public void testValidateTokenSuccess() {
        // Arrange
        String token = "validToken";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        
        // Act
        Boolean result = authService.validateToken(token);
        
        // Assert
        assertTrue(result);
        
        verify(tokenProvider).validateToken(token);
    }
    
    @Test
    public void testValidateTokenFailure() {
        // Arrange
        String token = "invalidToken";
        when(tokenProvider.validateToken(token)).thenReturn(false);
        
        // Act
        Boolean result = authService.validateToken(token);
        
        // Assert
        assertFalse(result);
        
        verify(tokenProvider).validateToken(token);
    }
}
