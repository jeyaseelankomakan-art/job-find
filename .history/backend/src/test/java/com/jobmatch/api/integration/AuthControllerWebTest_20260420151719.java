package com.jobmatch.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.api.controller.AuthController;
import com.jobmatch.api.model.dto.AuthResponse;
import com.jobmatch.api.model.dto.LoginRequest;
import com.jobmatch.api.model.dto.RegisterRequest;
import com.jobmatch.api.model.entity.User;
import com.jobmatch.api.model.entity.User.UserRole;
import com.jobmatch.api.model.entity.User.UserStatus;
import com.jobmatch.api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void registerReturnsCreatedResponse() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "new.user@example.com",
                "Password123!",
                "New User",
                "1234567890",
                "JOB_SEEKER"
        );

        User user = User.builder()
                .id(101L)
                .email("new.user@example.com")
                .fullName("New User")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.id").value(101));
    }

    @Test
    void loginReturnsTokenPayload() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "Password123!");
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(86400L)
                .user(User.builder().id(5L).email("user@example.com").fullName("User").build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }
}