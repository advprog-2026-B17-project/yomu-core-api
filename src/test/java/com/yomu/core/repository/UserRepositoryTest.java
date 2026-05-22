package com.yomu.core.repository;

import com.yomu.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepository.
 * Uses H2 in-memory database for isolation.
 * These tests verify that Spring Data JPA query methods work correctly.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setUsername("alice");
        alice.setEmail("alice@example.com");
        alice.setPhone("+62812345678");
        alice.setDisplayName("Alice");
        alice.setPasswordHash("hash");
        alice.setRole("student");
        em.persist(alice);

        bob = new User();
        bob.setUsername("bob");
        bob.setEmail("bob@example.com");
        bob.setDisplayName("Bob");
        bob.setPasswordHash("hash");
        bob.setRole("student");
        em.persist(bob);

        em.flush();
    }

    // ===== findByUsername =====

    @Nested
    @DisplayName("findByUsername()")
    class FindByUsername {

        @Test
        @DisplayName("returns user when exists")
        void returnsUserWhenExists() {
            Optional<User> result = userRepository.findByUsername("alice");

            assertTrue(result.isPresent());
            assertEquals("alice@example.com", result.get().getEmail());
        }

        @Test
        @DisplayName("returns empty when not exists")
        void returnsEmptyWhenNotExists() {
            Optional<User> result = userRepository.findByUsername("unknown");

            assertTrue(result.isEmpty());
        }
    }

    // ===== findByEmail =====

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("returns user when exists")
        void returnsUserWhenExists() {
            Optional<User> result = userRepository.findByEmail("alice@example.com");

            assertTrue(result.isPresent());
            assertEquals("alice", result.get().getUsername());
        }

        @Test
        @DisplayName("returns empty when not exists")
        void returnsEmptyWhenNotExists() {
            Optional<User> result = userRepository.findByEmail("unknown@example.com");

            assertTrue(result.isEmpty());
        }
    }

    // ===== findByPhone =====

    @Nested
    @DisplayName("findByPhone()")
    class FindByPhone {

        @Test
        @DisplayName("returns user when phone matches")
        void returnsUserWhenPhoneMatches() {
            Optional<User> result = userRepository.findByPhone("+62812345678");

            assertTrue(result.isPresent());
            assertEquals("alice", result.get().getUsername());
        }

        @Test
        @DisplayName("returns empty when phone not found")
        void returnsEmptyWhenPhoneNotFound() {
            Optional<User> result = userRepository.findByPhone("+62999999999");

            assertTrue(result.isEmpty());
        }
    }

    // ===== findFirstByUsernameOrEmailOrPhone =====

    @Nested
    @DisplayName("findFirstByUsernameOrEmailOrPhone()")
    class FindFirstByUsernameOrEmailOrPhone {

        @Test
        @DisplayName("finds by username")
        void findsByUsername() {
            Optional<User> result = userRepository.findFirstByUsernameOrEmailOrPhone(
                    "alice", "unknown@example.com", "+62999999999");

            assertTrue(result.isPresent());
            assertEquals("alice", result.get().getUsername());
        }

        @Test
        @DisplayName("finds by email when username not found")
        void findsByEmailWhenUsernameNotFound() {
            Optional<User> result = userRepository.findFirstByUsernameOrEmailOrPhone(
                    "unknown", "alice@example.com", "+62999999999");

            assertTrue(result.isPresent());
            assertEquals("alice", result.get().getUsername());
        }

        @Test
        @DisplayName("finds by phone when username and email not found")
        void findsByPhoneWhenOthersNotFound() {
            Optional<User> result = userRepository.findFirstByUsernameOrEmailOrPhone(
                    "unknown", "unknown@example.com", "+62812345678");

            assertTrue(result.isPresent());
            assertEquals("alice", result.get().getUsername());
        }

        @Test
        @DisplayName("returns empty when nothing matches")
        void returnsEmptyWhenNothingMatches() {
            Optional<User> result = userRepository.findFirstByUsernameOrEmailOrPhone(
                    "unknown", "unknown@example.com", "+62999999999");

            assertTrue(result.isEmpty());
        }
    }

    // ===== findByGoogleId =====

    @Nested
    @DisplayName("findByGoogleId()")
    class FindByGoogleId {

        @Test
        @DisplayName("returns user when googleId matches")
        void returnsUserWhenGoogleIdMatches() {
            alice.setGoogleId("google-123");
            em.persist(alice);
            em.flush();

            Optional<User> result = userRepository.findByGoogleId("google-123");

            assertTrue(result.isPresent());
            assertEquals("alice", result.get().getUsername());
        }

        @Test
        @DisplayName("returns empty when googleId not found")
        void returnsEmptyWhenNotFound() {
            Optional<User> result = userRepository.findByGoogleId("google-unknown");

            assertTrue(result.isEmpty());
        }
    }

    // ===== existsByUsername =====

    @Nested
    @DisplayName("existsByUsername()")
    class ExistsByUsername {

        @Test
        @DisplayName("returns true when username exists")
        void returnsTrueWhenExists() {
            assertTrue(userRepository.existsByUsername("alice"));
        }

        @Test
        @DisplayName("returns false when username not exists")
        void returnsFalseWhenNotExists() {
            assertFalse(userRepository.existsByUsername("unknown"));
        }
    }

    // ===== existsByEmail =====

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("returns true when email exists")
        void returnsTrueWhenExists() {
            assertTrue(userRepository.existsByEmail("alice@example.com"));
        }

        @Test
        @DisplayName("returns false when email not exists")
        void returnsFalseWhenNotExists() {
            assertFalse(userRepository.existsByEmail("unknown@example.com"));
        }
    }

    // ===== existsByPhone =====

    @Nested
    @DisplayName("existsByPhone()")
    class ExistsByPhone {

        @Test
        @DisplayName("returns true when phone exists")
        void returnsTrueWhenExists() {
            assertTrue(userRepository.existsByPhone("+62812345678"));
        }

        @Test
        @DisplayName("returns false when phone not exists")
        void returnsFalseWhenNotExists() {
            assertFalse(userRepository.existsByPhone("+62999999999"));
        }

        @Test
        @DisplayName("returns false when phone is empty string")
        void returnsFalseWhenPhoneEmpty() {
            // Empty string is not a valid phone, should not match any record
            assertFalse(userRepository.existsByPhone(""));
        }
    }
}