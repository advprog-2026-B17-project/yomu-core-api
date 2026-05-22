package com.yomu.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yomu.core.dto.UpdateUserRequest;
import com.yomu.core.dto.UserDTO;
import com.yomu.core.dto.UserProfileDTO;
import com.yomu.core.dto.StatsDTO;
import com.yomu.core.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private static final String USERS_BASE = "/api/users";

    private UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private UUID otherUserId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        // Reset any state
    }

    private RequestPostProcessor userAuth(UUID userId, String role) {
        return request -> {
            var auth = new UsernamePasswordAuthenticationToken(
                    userId.toString(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            request.setUserPrincipal(auth);
            return request;
        };
    }

    // ===== /me =====

    @Nested
    @DisplayName("GET /api/users/me")
    class GetCurrentUser {

        @Test
        @DisplayName("returns 200 with user data when authenticated")
        void returns200WhenAuthenticated() throws Exception {
            UserDTO user = new UserDTO(
                    userId.toString(), "alice", "alice@example.com",
                    null, "Alice", "student"
            );

            when(userService.getUserById(userId)).thenReturn(Optional.of(user));

            mockMvc.perform(get(USERS_BASE + "/me")
                            .with(userAuth(userId, "STUDENT")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("alice"))
                    .andExpect(jsonPath("$.displayName").value("Alice"))
                    .andExpect(jsonPath("$.role").value("student"));
        }

        @Test
        @DisplayName("returns 404 when user not found")
        void returns404WhenUserNotFound() throws Exception {
            when(userService.getUserById(userId)).thenReturn(Optional.empty());

            mockMvc.perform(get(USERS_BASE + "/me")
                            .with(userAuth(userId, "STUDENT")))
                    .andExpect(status().isNotFound());
        }
    }

    // ===== /{id}/profile =====

    @Nested
    @DisplayName("GET /api/users/{id}/profile")
    class GetUserProfile {

        @Test
        @DisplayName("returns 200 with profile for own profile")
        void returns200ForOwnProfile() throws Exception {
            UserProfileDTO profile = new UserProfileDTO(
                    Map.of("username", "alice", "displayName", "Alice"),
                    new StatsDTO(10, 5, 85.0)
            );

            when(userService.getUserProfile(userId)).thenReturn(profile);

            mockMvc.perform(get(USERS_BASE + "/" + userId + "/profile")
                            .with(userAuth(userId, "STUDENT")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.displayName").value("Alice"))
                    .andExpect(jsonPath("$.stats.readingsCompleted").value(10));
        }

        @Test
        @DisplayName("returns 200 with profile for other user")
        void returns200ForOtherUser() throws Exception {
            UserProfileDTO profile = new UserProfileDTO(
                    Map.of("username", "bob", "displayName", "Bob"),
                    new StatsDTO(5, 2, 70.0)
            );

            when(userService.getUserProfile(otherUserId)).thenReturn(profile);

            mockMvc.perform(get(USERS_BASE + "/" + otherUserId + "/profile")
                            .with(userAuth(userId, "STUDENT")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.displayName").value("Bob"));
        }
    }

    // ===== /{id} PUT =====

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUser {

        @Test
        @DisplayName("returns 200 when updating own profile")
        void returns200WhenUpdatingOwnProfile() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest();
            request.setDisplayName("Alice Updated");

            UserDTO updated = new UserDTO(
                    userId.toString(), "alice", "alice@example.com",
                    null, "Alice Updated", "student"
            );

            when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                    .thenReturn(Optional.of(updated));

            mockMvc.perform(put(USERS_BASE + "/" + userId)
                            .with(userAuth(userId, "STUDENT"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.displayName").value("Alice Updated"));
        }

        @Test
        @DisplayName("returns 403 when updating other user's profile")
        void returns403WhenUpdatingOtherUser() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest();
            request.setDisplayName("Bob Updated");

            mockMvc.perform(put(USERS_BASE + "/" + otherUserId)
                            .with(userAuth(userId, "STUDENT"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(userService, never()).updateUser(any(), any());
        }

        @Test
        @DisplayName("returns 200 when admin updates other user's profile")
        void returns200WhenAdminUpdatesOtherUser() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest();
            request.setDisplayName("Bob Updated By Admin");

            UserDTO updated = new UserDTO(
                    otherUserId.toString(), "bob", "bob@example.com",
                    null, "Bob Updated By Admin", "student"
            );

            when(userService.updateUser(eq(otherUserId), any(UpdateUserRequest.class)))
                    .thenReturn(Optional.of(updated));

            mockMvc.perform(put(USERS_BASE + "/" + otherUserId)
                            .with(userAuth(otherUserId, "ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.displayName").value("Bob Updated By Admin"));
        }

        @Test
        @DisplayName("returns 400 when service throws validation error")
        void returns400WhenServiceThrowsValidationError() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest();
            request.setUsername("bob");

            when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                    .thenThrow(new RuntimeException("Username already taken"));

            mockMvc.perform(put(USERS_BASE + "/" + userId)
                            .with(userAuth(userId, "STUDENT"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Username already taken"));
        }

        @Test
        @DisplayName("returns 404 when user to update not found")
        void returns404WhenUserNotFound() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest();
            request.setDisplayName("Alice Updated");

            when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(put(USERS_BASE + "/" + userId)
                            .with(userAuth(userId, "STUDENT"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ===== /{id} DELETE =====

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("returns 204 when deleting own profile")
        void returns204WhenDeletingOwnProfile() throws Exception {
            when(userService.deleteUser(userId)).thenReturn(true);

            mockMvc.perform(delete(USERS_BASE + "/" + userId)
                            .with(userAuth(userId, "STUDENT")))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(userId);
        }

        @Test
        @DisplayName("returns 403 when deleting other user's profile")
        void returns403WhenDeletingOtherUser() throws Exception {
            mockMvc.perform(delete(USERS_BASE + "/" + otherUserId)
                            .with(userAuth(userId, "STUDENT")))
                    .andExpect(status().isForbidden());

            verify(userService, never()).deleteUser(any());
        }

        @Test
        @DisplayName("returns 204 when admin deletes other user's profile")
        void returns204WhenAdminDeletesOtherUser() throws Exception {
            when(userService.deleteUser(otherUserId)).thenReturn(true);

            mockMvc.perform(delete(USERS_BASE + "/" + otherUserId)
                            .with(userAuth(otherUserId, "ADMIN")))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when deleting non-existent user")
        void returns404WhenUserNotFound() throws Exception {
            when(userService.deleteUser(userId)).thenReturn(false);

            mockMvc.perform(delete(USERS_BASE + "/" + userId)
                            .with(userAuth(userId, "STUDENT")))
                    .andExpect(status().isNotFound());
        }
    }
}
