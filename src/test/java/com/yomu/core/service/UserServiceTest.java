package com.yomu.core.service;

import com.yomu.core.dto.*;
import com.yomu.core.entity.*;
import com.yomu.core.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CompletedReadingRepository completedReadingRepository;
    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private UserDeletionService userDeletionService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                completedReadingRepository,
                quizAttemptRepository,
                userDeletionService
        );
    }

    // ===== findById() =====

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));

            Optional<User> result = userService.findById(id);

            assertTrue(result.isPresent());
            assertEquals("alice", result.get().getUsername());
        }

        @Test
        @DisplayName("returns empty when not found")
        void returnsEmptyWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            Optional<User> result = userService.findById(id);

            assertTrue(result.isEmpty());
        }
    }

    // ===== getUserById() =====

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("returns user DTO when found")
        void returnsUserDtoWhenFound() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setDisplayName("Alice Smith");
            user.setRole("student");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));

            Optional<UserDTO> result = userService.getUserById(id);

            assertTrue(result.isPresent());
            assertEquals("alice", result.get().getUsername());
            assertEquals("alice@example.com", result.get().getEmail());
        }

        @Test
        @DisplayName("returns empty when not found")
        void returnsEmptyWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            Optional<UserDTO> result = userService.getUserById(id);

            assertTrue(result.isEmpty());
        }
    }

    // ===== getUserProfile() =====

    @Nested
    @DisplayName("getUserProfile()")
    class GetUserProfile {

        @Test
        @DisplayName("returns profile with correct stats")
        void returnsProfileWithCorrectStats() {
            UUID userId = UUID.randomUUID();

            User user = new User();
            user.setId(userId);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setDisplayName("Alice Smith");
            user.setRole("student");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(completedReadingRepository.countByUserId(userId)).thenReturn(5L);
            when(quizAttemptRepository.countByUserId(userId)).thenReturn(3L);
            when(quizAttemptRepository.getAverageAccuracyByUserId(userId)).thenReturn(0.85);

            UserProfileDTO profile = userService.getUserProfile(userId);

            assertNotNull(profile);
            assertEquals(5L, profile.getStats().getReadingsCompleted());
            assertEquals(3L, profile.getStats().getQuizzesTaken());
            assertEquals(0.85, profile.getStats().getAverageAccuracy());
        }

        @Test
        @DisplayName("handles zero stats")
        void handlesZeroStats() {
            UUID userId = UUID.randomUUID();

            User user = new User();
            user.setId(userId);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setDisplayName("Alice");
            user.setRole("student");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(completedReadingRepository.countByUserId(userId)).thenReturn(0L);
            when(quizAttemptRepository.countByUserId(userId)).thenReturn(0L);
            when(quizAttemptRepository.getAverageAccuracyByUserId(userId)).thenReturn(null);

            UserProfileDTO profile = userService.getUserProfile(userId);

            assertNotNull(profile);
            assertEquals(0L, profile.getStats().getReadingsCompleted());
            assertEquals(0L, profile.getStats().getQuizzesTaken());
            assertEquals(0.0, profile.getStats().getAverageAccuracy());
        }

        @Test
        @DisplayName("handles null average accuracy")
        void handlesNullAccuracy() {
            UUID userId = UUID.randomUUID();

            User user = new User();
            user.setId(userId);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setDisplayName("Alice");
            user.setRole("student");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(completedReadingRepository.countByUserId(userId)).thenReturn(0L);
            when(quizAttemptRepository.countByUserId(userId)).thenReturn(0L);
            when(quizAttemptRepository.getAverageAccuracyByUserId(userId)).thenReturn(null);

            UserProfileDTO profile = userService.getUserProfile(userId);

            assertEquals(0.0, profile.getStats().getAverageAccuracy());
        }
    }
}