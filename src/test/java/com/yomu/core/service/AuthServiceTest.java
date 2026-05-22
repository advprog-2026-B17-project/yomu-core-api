package com.yomu.core.service;

import com.yomu.core.dto.AuthResponse;
import com.yomu.core.dto.GoogleAuthRequest;
import com.yomu.core.dto.LoginRequest;
import com.yomu.core.dto.RegisterRequest;
import com.yomu.core.entity.User;
import com.yomu.core.repository.UserRepository;
import com.yomu.core.security.GoogleTokenValidator;
import com.yomu.core.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GoogleTokenValidator googleTokenValidator;

    @Mock
    private EventPublisher eventPublisher;

    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    private static final String FAKE_TOKEN = "jwt-token-fake";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm",
                3600000L
        );
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtTokenProvider,
                googleTokenValidator,
                eventPublisher
        );
    }

    // ===== register() =====

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("registers new user successfully")
        void registersNewUser() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("alice");
            request.setEmail("alice@example.com");
            request.setDisplayName("Alice");
            request.setPassword("password123");

            // Stub in specific order: concrete calls BEFORE any() calls
            when(userRepository.existsByUsername("alice")).thenReturn(false);
            when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashedpassword");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });

            AuthResponse response = authService.register(request);

            assertNotNull(response);
            assertNotNull(response.getToken());
            assertTrue(response.getToken().length() > 20); // JWT format
            assertEquals("alice", response.getUsername());
            assertEquals("Alice", response.getDisplayName());
            assertEquals("student", response.getRole());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("throws when username already taken")
        void throwsWhenUsernameTaken() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("bob");
            request.setEmail("bob@example.com");
            request.setDisplayName("Bob");
            request.setPassword("password123");

            when(userRepository.existsByUsername("bob")).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
            assertEquals("Username already taken", ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("throws when email already registered")
        void throwsWhenEmailRegistered() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("carol");
            request.setEmail("carol@example.com");
            request.setDisplayName("Carol");
            request.setPassword("password123");

            when(userRepository.existsByUsername("carol")).thenReturn(false);
            when(userRepository.existsByEmail("carol@example.com")).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
            assertEquals("Email already registered", ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("throws when phone already registered")
        void throwsWhenPhoneRegistered() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("dave");
            request.setEmail("dave@example.com");
            request.setPhone("+62812345678");
            request.setDisplayName("Dave");
            request.setPassword("password123");

            when(userRepository.existsByUsername("dave")).thenReturn(false);
            when(userRepository.existsByEmail("dave@example.com")).thenReturn(false);
            when(userRepository.existsByPhone("+62812345678")).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
            assertEquals("Phone already registered", ex.getMessage());
        }

        @Test
        @DisplayName("skips phone uniqueness check when phone is blank")
        void skipsPhoneCheckWhenBlank() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("eve");
            request.setEmail("eve@example.com");
            request.setPhone("");
            request.setDisplayName("Eve");
            request.setPassword("password123");

            when(userRepository.existsByUsername("eve")).thenReturn(false);
            when(userRepository.existsByEmail("eve@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });

            AuthResponse response = authService.register(request);

            assertNotNull(response);
            verify(userRepository, never()).existsByPhone(any(String.class));
        }

        @Test
        @DisplayName("sets role to student by default")
        void setsRoleToStudent() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("frank");
            request.setEmail("frank@example.com");
            request.setDisplayName("Frank");
            request.setPassword("password123");

            when(userRepository.existsByUsername("frank")).thenReturn(false);
            when(userRepository.existsByEmail("frank@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });

            AuthResponse response = authService.register(request);

            assertEquals("student", response.getRole());
        }
    }

    // ===== login() =====

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("returns token for valid username+password")
        void returnsTokenForValidCredentials() {
            LoginRequest request = new LoginRequest();
            request.setUsername("alice");
            request.setPassword("password123");

            User user = new User();
            user.setId(UUID.randomUUID());
            user.setUsername("alice");
            user.setEmail("alice@example.com");
            user.setDisplayName("Alice");
            user.setRole("student");
            user.setPasswordHash("hashedpassword");

            when(userRepository.findFirstByUsernameOrEmailOrPhone("alice", "alice", "alice"))
                    .thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "hashedpassword")).thenReturn(true);

            AuthResponse response = authService.login(request);

            assertNotNull(response);
            assertNotNull(response.getToken());
            assertTrue(response.getToken().length() > 20); // JWT format
            assertEquals("alice", response.getUsername());
        }

        @Test
        @DisplayName("throws when user not found")
        void throwsWhenUserNotFound() {
            LoginRequest request = new LoginRequest();
            request.setUsername("unknown");
            request.setPassword("password123");

            when(userRepository.findFirstByUsernameOrEmailOrPhone("unknown", "unknown", "unknown"))
                    .thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
            assertEquals("Invalid credentials", ex.getMessage());
        }

        @Test
        @DisplayName("throws when password does not match")
        void throwsWhenPasswordMismatch() {
            LoginRequest request = new LoginRequest();
            request.setUsername("alice");
            request.setPassword("wrongpassword");

            User user = new User();
            user.setId(UUID.randomUUID());
            user.setUsername("alice");
            user.setDisplayName("Alice");
            user.setRole("student");
            user.setPasswordHash("hashedpassword");

            when(userRepository.findFirstByUsernameOrEmailOrPhone("alice", "alice", "alice"))
                    .thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpassword", "hashedpassword")).thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
            assertEquals("Invalid credentials", ex.getMessage());
        }
    }

    // ===== findById() =====

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setId(userId);
            user.setUsername("alice");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            Optional<User> result = authService.findById(userId.toString());

            assertTrue(result.isPresent());
            assertEquals("alice", result.get().getUsername());
        }

        @Test
        @DisplayName("returns empty when user not found")
        void returnsEmptyWhenNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            Optional<User> result = authService.findById(userId.toString());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("throws when UUID is invalid")
        void throwsWhenInvalidUUID() {
            assertThrows(Exception.class, () -> authService.findById("not-a-uuid"));
        }
    }

    // ===== googleAuth() =====

    @Nested
    @DisplayName("googleAuth()")
    class GoogleAuth {

        @Test
        @DisplayName("creates new user when Google user does not exist")
        void createsNewUserIfNotExists() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("alice@gmail.com");
            request.setDisplayName("Alice Google");
            request.setGoogleId("google-123");
            request.setIdToken("some-id-token"); // will be ignored when GOOGLE_CLIENT_ID is empty

            when(userRepository.findByEmail("alice@gmail.com")).thenReturn(Optional.empty());
            when(userRepository.existsByUsername(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("random-hash");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });

            AuthResponse response = authService.googleAuth(request);

            assertNotNull(response);
            assertNotNull(response.getToken());
            assertTrue(response.getToken().length() > 20); // JWT format
            assertEquals("alice", response.getUsername()); // username derived from email prefix
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("returns existing user when Google user already registered")
        void returnsExistingUser() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("alice@gmail.com");
            request.setDisplayName("Alice Google");
            request.setGoogleId("google-123");

            User existingUser = new User();
            existingUser.setId(UUID.randomUUID());
            existingUser.setUsername("alice_google");
            existingUser.setEmail("alice@gmail.com");
            existingUser.setDisplayName("Alice Google");
            existingUser.setGoogleId("google-123");
            existingUser.setRole("student");

            when(userRepository.findByEmail("alice@gmail.com")).thenReturn(Optional.of(existingUser));

            AuthResponse response = authService.googleAuth(request);

            assertNotNull(response);
            assertNotNull(response.getToken());
            assertTrue(response.getToken().length() > 20); // JWT format
            assertEquals("alice_google", response.getUsername());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("updates googleId if not set on existing user")
        void updatesGoogleIdIfNotSet() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("alice@gmail.com");
            request.setDisplayName("Alice Google");
            request.setGoogleId("google-123");

            User existingUser = new User();
            existingUser.setId(UUID.randomUUID());
            existingUser.setUsername("alice");
            existingUser.setEmail("alice@gmail.com");
            existingUser.setDisplayName("Alice Google");
            existingUser.setGoogleId(null);  // not linked yet
            existingUser.setRole("student");

            when(userRepository.findByEmail("alice@gmail.com")).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            AuthResponse response = authService.googleAuth(request);

            verify(userRepository).save(any(User.class));  // called to update googleId
        }

        @Test
        @DisplayName("throws when email is blank")
        void throwsWhenEmailBlank() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setGoogleId("google-123");

            doThrow(new RuntimeException("Google email and subject are required"))
                    .when(googleTokenValidator).validate(any(GoogleAuthRequest.class));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.googleAuth(request));
            assertEquals("Google email and subject are required", ex.getMessage());
        }

        @Test
        @DisplayName("throws when googleId is blank")
        void throwsWhenGoogleIdBlank() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("alice@gmail.com");

            doThrow(new RuntimeException("Google email and subject are required"))
                    .when(googleTokenValidator).validate(any(GoogleAuthRequest.class));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.googleAuth(request));
            assertEquals("Google email and subject are required", ex.getMessage());
        }
    }
}