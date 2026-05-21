package com.yomu.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yomu.core.dto.AuthResponse;
import com.yomu.core.dto.GoogleAuthRequest;
import com.yomu.core.dto.LoginRequest;
import com.yomu.core.dto.RegisterRequest;
import com.yomu.core.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private static final String AUTH_BASE = "/api/auth";

    // ===== /register =====

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("returns 200 with token on success")
        void returns200OnSuccess() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("alice");
            request.setEmail("alice@example.com");
            request.setDisplayName("Alice");
            request.setPassword("password123");

            AuthResponse response = new AuthResponse(
                    "jwt-token", UUID.randomUUID().toString(), "alice", "Alice", "student"
            );

            when(authService.register(any(RegisterRequest.class))).thenReturn(response);

            mockMvc.perform(post(AUTH_BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.username").value("alice"))
                    .andExpect(jsonPath("$.displayName").value("Alice"))
                    .andExpect(jsonPath("$.role").value("student"));
        }

        @Test
        @DisplayName("returns 400 when username is blank")
        void returns400WhenUsernameBlank() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("");
            request.setEmail("alice@example.com");
            request.setDisplayName("Alice");
            request.setPassword("password123");

            mockMvc.perform(post(AUTH_BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when email is invalid")
        void returns400WhenEmailInvalid() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("alice");
            request.setEmail("not-an-email");
            request.setDisplayName("Alice");
            request.setPassword("password123");

            mockMvc.perform(post(AUTH_BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when password is too short")
        void returns400WhenPasswordTooShort() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("alice");
            request.setEmail("alice@example.com");
            request.setDisplayName("Alice");
            request.setPassword("short");

            mockMvc.perform(post(AUTH_BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when service throws RuntimeException")
        void returns400WhenServiceThrows() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("alice");
            request.setEmail("alice@example.com");
            request.setDisplayName("Alice");
            request.setPassword("password123");

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new RuntimeException("Username already taken"));

            mockMvc.perform(post(AUTH_BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Username already taken"));
        }
    }

    // ===== /login =====

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("returns 200 with token on valid credentials")
        void returns200OnValidCredentials() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("alice");
            request.setPassword("password123");

            AuthResponse response = new AuthResponse(
                    "jwt-token", UUID.randomUUID().toString(), "alice", "Alice", "student"
            );

            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post(AUTH_BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.username").value("alice"));
        }

        @Test
        @DisplayName("returns 400 when username is blank")
        void returns400WhenUsernameBlank() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("");
            request.setPassword("password123");

            mockMvc.perform(post(AUTH_BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when service throws invalid credentials")
        void returns400WhenInvalidCredentials() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("alice");
            request.setPassword("wrongpassword");

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("Invalid credentials"));

            mockMvc.perform(post(AUTH_BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));
        }
    }

    // ===== /google =====

    @Nested
    @DisplayName("POST /api/auth/google")
    class GoogleAuth {

        @Test
        @DisplayName("returns 200 with token on valid Google auth")
        void returns200OnValidGoogleAuth() throws Exception {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("alice@gmail.com");
            request.setDisplayName("Alice Google");
            request.setGoogleId("google-123");

            AuthResponse response = new AuthResponse(
                    "jwt-token", UUID.randomUUID().toString(), "alice_google", "Alice Google", "student"
            );

            when(authService.googleAuth(any(GoogleAuthRequest.class))).thenReturn(response);

            mockMvc.perform(post(AUTH_BASE + "/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.username").value("alice_google"))
                    .andExpect(jsonPath("$.displayName").value("Alice Google"));
        }

        @Test
        @DisplayName("returns 400 when Google auth fails")
        void returns400WhenGoogleAuthFails() throws Exception {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("alice@gmail.com");
            request.setDisplayName("Alice Google");
            request.setGoogleId("google-123");

            when(authService.googleAuth(any(GoogleAuthRequest.class)))
                    .thenThrow(new RuntimeException("Google email and subject are required"));

            mockMvc.perform(post(AUTH_BASE + "/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Google email and subject are required"));
        }
    }
}