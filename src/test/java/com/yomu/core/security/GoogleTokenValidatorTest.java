package com.yomu.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yomu.core.dto.GoogleAuthRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoogleTokenValidatorTest {

    private GoogleTokenValidator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validator = new GoogleTokenValidator(objectMapper, "test-client-id");
    }

    @Nested
    @DisplayName("validate()")
    class Validate {

        @Test
        @DisplayName("throws when email is blank")
        void throwsWhenEmailBlank() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("");
            request.setGoogleId("google-123");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> validator.validate(request));
            assertEquals("Google email and subject are required", ex.getMessage());
        }

        @Test
        @DisplayName("throws when googleId is blank")
        void throwsWhenGoogleIdBlank() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("test@example.com");
            request.setGoogleId("");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> validator.validate(request));
            assertEquals("Google email and subject are required", ex.getMessage());
        }

        @Test
        @DisplayName("does not throw when only email and googleId are provided and googleClientId is empty")
        void doesNotValidateWhenNoGoogleClientId() {
            GoogleTokenValidator validatorNoClientId = new GoogleTokenValidator(objectMapper, "");

            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("test@example.com");
            request.setGoogleId("google-123");

            assertDoesNotThrow(() -> validatorNoClientId.validate(request));
        }

        @Test
        @DisplayName("throws when googleClientId is set but idToken is missing")
        void throwsWhenIdTokenMissing() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("test@example.com");
            request.setGoogleId("google-123");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> validator.validate(request));
            assertEquals("Missing Google ID token", ex.getMessage());
        }

        @Test
        @DisplayName("does not throw for null email")
        void doesNotThrowForNullEmail() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail(null);
            request.setGoogleId("google-123");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> validator.validate(request));
            assertEquals("Google email and subject are required", ex.getMessage());
        }

        @Test
        @DisplayName("does not throw for null googleId")
        void doesNotThrowForNullGoogleId() {
            GoogleAuthRequest request = new GoogleAuthRequest();
            request.setEmail("test@example.com");
            request.setGoogleId(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> validator.validate(request));
            assertEquals("Google email and subject are required", ex.getMessage());
        }
    }

    }