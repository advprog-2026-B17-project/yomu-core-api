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
import static org.mockito.ArgumentMatchers.any;
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
    private UserAchievementRepository userAchievementRepository;
    @Mock
    private AchievementRepository achievementRepository;
    @Mock
    private ClanMemberRepository clanMemberRepository;
    @Mock
    private ClanRepository clanRepository;
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
                userAchievementRepository,
                achievementRepository,
                clanMemberRepository,
                clanRepository,
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
        @DisplayName("returns UserDTO when user found")
        void returnsUserDTOWhenFound() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setDisplayName("Alice");
            user.setRole("student");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));

            Optional<UserDTO> result = userService.getUserById(id);

            assertTrue(result.isPresent());
            assertEquals("alice", result.get().getUsername());
            assertEquals("alice@example.com", result.get().getEmail());
            assertEquals("student", result.get().getRole());
        }

        @Test
        @DisplayName("returns UserDTO with phone when user has phone")
        void returnsUserDTOWithPhone() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setPhone("+62812345678");
            user.setDisplayName("Alice");
            user.setRole("student");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));

            Optional<UserDTO> result = userService.getUserById(id);

            assertTrue(result.isPresent());
            assertEquals("+62812345678", result.get().getPhone());
        }

        @Test
        @DisplayName("returns empty when user not found")
        void returnsEmptyWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            Optional<UserDTO> result = userService.getUserById(id);

            assertTrue(result.isEmpty());
        }
    }

    // ===== updateUser() =====

    @Nested
    @DisplayName("updateUser()")
    class UpdateUser {

        @Test
        @DisplayName("updates displayName successfully")
        void updatesDisplayName() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setDisplayName("Alice Old");

            UpdateUserRequest request = new UpdateUserRequest();
            request.setDisplayName("Alice New");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            Optional<UserDTO> result = userService.updateUser(id, request);

            assertTrue(result.isPresent());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("throws when new username already taken by another user")
        void throwsWhenUsernameConflict() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");

            User existingUser = new User();
            existingUser.setId(UUID.randomUUID());

            UpdateUserRequest request = new UpdateUserRequest();
            request.setUsername("bob");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.findByUsername("bob")).thenReturn(Optional.of(existingUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> userService.updateUser(id, request));
            assertEquals("Username already taken", ex.getMessage());
        }

        @Test
        @DisplayName("throws when new email already registered by another user")
        void throwsWhenEmailConflict() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");

            User existingUser = new User();
            existingUser.setId(UUID.randomUUID());

            UpdateUserRequest request = new UpdateUserRequest();
            request.setEmail("bob@example.com");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(existingUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> userService.updateUser(id, request));
            assertEquals("Email already registered", ex.getMessage());
        }

        @Test
        @DisplayName("updates password when updatePassword flag is true")
        void updatesPasswordWhenFlagTrue() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");

            UpdateUserRequest request = new UpdateUserRequest();
            request.setPassword("newpassword");
            request.setUpdatePassword(true);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newpassword")).thenReturn("new-hash");
            when(userRepository.save(any(User.class))).thenReturn(user);

            Optional<UserDTO> result = userService.updateUser(id, request);

            assertTrue(result.isPresent());
            verify(passwordEncoder).encode("newpassword");
        }

        @Test
        @DisplayName("does not update password when updatePassword flag is false")
        void doesNotUpdatePasswordWhenFlagFalse() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setPasswordHash("old-hash");

            UpdateUserRequest request = new UpdateUserRequest();
            request.setPassword("newpassword");
            request.setUpdatePassword(false);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.updateUser(id, request);

            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("sets phone to null when blank phone is provided")
        void setsPhoneToNullWhenBlank() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setPhone("+62812345678");

            UpdateUserRequest request = new UpdateUserRequest();
            request.setPhone("   ");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.updateUser(id, request);

            assertNull(user.getPhone());
        }

        @Test
        @DisplayName("throws when new phone already registered by another user")
        void throwsWhenPhoneConflict() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setPhone("+62811111111");

            User existingUser = new User();
            existingUser.setId(UUID.randomUUID());

            UpdateUserRequest request = new UpdateUserRequest();
            request.setPhone("+62822222222");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.findByPhone("+62822222222")).thenReturn(Optional.of(existingUser));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> userService.updateUser(id, request));
            assertEquals("Phone already registered", ex.getMessage());
        }

        @Test
        @DisplayName("skips phone check when phone is not being updated")
        void skipsPhoneCheckWhenNotUpdated() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setPhone("+62812345678");

            UpdateUserRequest request = new UpdateUserRequest();
            request.setDisplayName("Alice New");

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.updateUser(id, request);

            verify(userRepository, never()).findByPhone(anyString());
        }
    }

    // ===== deleteUser() =====

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("deletes user and returns true when found")
        void deletesUserWhenFound() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));

            boolean result = userService.deleteUser(id);

            assertTrue(result);
            verify(userDeletionService).cascadeDelete(id);
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("deletes user and delegates cascade to UserDeletionService")
        void delegatesCascadeToDeletionService() {
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));

            userService.deleteUser(id);

            verify(userDeletionService).cascadeDelete(id);
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("returns false when user not found")
        void returnsFalseWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            boolean result = userService.deleteUser(id);

            assertFalse(result);
            verify(userRepository, never()).delete(any());
            verify(userDeletionService, never()).cascadeDelete(any());
        }
    }

    // ===== getUserProfile() =====

    @Nested
    @DisplayName("getUserProfile()")
    class GetUserProfile {

        @Test
        @DisplayName("returns profile with stats, achievements, and clan")
        void returnsFullProfile() {
            UUID userId = UUID.randomUUID();
            UUID achievementId = UUID.randomUUID();
            UUID clanId = UUID.randomUUID();

            User user = new User();
            user.setId(userId);
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setDisplayName("Alice");
            user.setRole("student");

            Achievement achievement = new Achievement();
            achievement.setId(achievementId);
            achievement.setName("First Read");

            UserAchievement ua = new UserAchievement();
            ua.setUserId(userId);
            ua.setAchievementId(achievementId);
            ua.setUnlockedAt(OffsetDateTime.now());

            Clan clan = new Clan();
            clan.setId(clanId);
            clan.setName("Dragons");
            clan.setTier("gold");

            ClanMember member = new ClanMember();
            member.setUserId(userId);
            member.setClanId(clanId);
            member.setRole("leader");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(completedReadingRepository.countByUserId(userId)).thenReturn(10L);
            when(quizAttemptRepository.countByUserId(userId)).thenReturn(5L);
            when(quizAttemptRepository.getAverageAccuracyByUserId(userId)).thenReturn(85.5);
            when(userAchievementRepository.findByUserIdAndIsVisibleTrueOrderByUnlockedAtDesc(userId))
                    .thenReturn(List.of(ua));
            when(achievementRepository.findAllById(List.of(achievementId))).thenReturn(List.of(achievement));
            when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
            when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));

            UserProfileDTO profile = userService.getUserProfile(userId, false);

            assertNotNull(profile);
            assertEquals("Alice", profile.getUser().get("displayName"));
            assertEquals(10L, profile.getStats().getReadingsCompleted());
            assertEquals(5L, profile.getStats().getQuizzesTaken());
            assertEquals(85.5, profile.getStats().getAverageAccuracy());
            assertEquals(1, profile.getAchievements().size());
            assertEquals("Dragons", profile.getClan().get("name"));
        }

        @Test
        @DisplayName("returns profile without hidden achievements when includeHiddenAchievements is false")
        void excludesHiddenAchievements() {
            UUID userId = UUID.randomUUID();
            UUID achievementId = UUID.randomUUID();

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
            when(userAchievementRepository.findByUserIdAndIsVisibleTrueOrderByUnlockedAtDesc(userId)).thenReturn(List.of());
            when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of());

            UserProfileDTO profile = userService.getUserProfile(userId, false);

            assertNotNull(profile);
            assertTrue(profile.getAchievements().isEmpty());
            verify(userAchievementRepository).findByUserIdAndIsVisibleTrueOrderByUnlockedAtDesc(userId);
            verify(userAchievementRepository, never()).findByUserIdOrderByUnlockedAtDesc(any());
        }

        @Test
        @DisplayName("returns profile with all achievements when includeHiddenAchievements is true")
        void includesHiddenAchievements() {
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
            when(userAchievementRepository.findByUserIdOrderByUnlockedAtDesc(userId)).thenReturn(List.of());
            when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of());

            userService.getUserProfile(userId, true);

            verify(userAchievementRepository).findByUserIdOrderByUnlockedAtDesc(userId);
            verify(userAchievementRepository, never()).findByUserIdAndIsVisibleTrueOrderByUnlockedAtDesc(any());
        }

        @Test
        @DisplayName("returns profile with null clan when user has no clan")
        void returnsNullClanWhenNoClan() {
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
            when(userAchievementRepository.findByUserIdAndIsVisibleTrueOrderByUnlockedAtDesc(userId)).thenReturn(List.of());
            when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of());

            UserProfileDTO profile = userService.getUserProfile(userId, false);

            assertNull(profile.getClan());
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
            when(userAchievementRepository.findByUserIdAndIsVisibleTrueOrderByUnlockedAtDesc(userId)).thenReturn(List.of());
            when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of());

            UserProfileDTO profile = userService.getUserProfile(userId, false);

            assertEquals(0.0, profile.getStats().getAverageAccuracy());
        }
    }
}
