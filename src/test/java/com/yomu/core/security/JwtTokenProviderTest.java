package com.yomu.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "this-is-a-very-long-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
    }

    // ===== generateToken() =====

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("generates a valid JWT token")
        void generatesValidToken() {
            String token = jwtTokenProvider.generateToken("user-123", "alice", "student");

            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertEquals(3, token.split("\\.").length); // JWT has 3 parts
        }

        @Test
        @DisplayName("token contains correct subject (userId)")
        void tokenContainsCorrectUserId() {
            String userId = UUID.randomUUID().toString();
            String token = jwtTokenProvider.generateToken(userId, "alice", "student");

            Claims claims = jwtTokenProvider.validateToken(token);

            assertEquals(userId, claims.getSubject());
        }

        @Test
        @DisplayName("token contains correct username claim")
        void tokenContainsCorrectUsername() {
            String token = jwtTokenProvider.generateToken("user-123", "alice", "student");

            Claims claims = jwtTokenProvider.validateToken(token);

            assertEquals("alice", claims.get("username", String.class));
        }

        @Test
        @DisplayName("token contains correct role claim")
        void tokenContainsCorrectRole() {
            String token = jwtTokenProvider.generateToken("user-123", "alice", "admin");

            Claims claims = jwtTokenProvider.validateToken(token);

            assertEquals("admin", claims.get("role", String.class));
        }

        @Test
        @DisplayName("token has expiration set")
        void tokenHasExpiration() {
            String token = jwtTokenProvider.generateToken("user-123", "alice", "student");

            Claims claims = jwtTokenProvider.validateToken(token);

            assertNotNull(claims.getExpiration());
            assertTrue(claims.getExpiration().after(new Date()));
        }

        @Test
        @DisplayName("token has issued at date set")
        void tokenHasIssuedAt() {
            String token = jwtTokenProvider.generateToken("user-123", "alice", "student");

            Claims claims = jwtTokenProvider.validateToken(token);

            assertNotNull(claims.getIssuedAt());
        }
    }

    // ===== validateToken() =====

    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("returns claims for valid token")
        void returnsClaimsForValidToken() {
            String token = jwtTokenProvider.generateToken("user-123", "bob", "student");

            Claims claims = jwtTokenProvider.validateToken(token);

            assertNotNull(claims);
            assertEquals("user-123", claims.getSubject());
        }

        @Test
        @DisplayName("throws for malformed token")
        void throwsForMalformedToken() {
            assertThrows(Exception.class, () -> jwtTokenProvider.validateToken("not.a.valid.jwt"));
        }

        @Test
        @DisplayName("throws for expired token")
        void throwsForExpiredToken() {
            // Create provider with -1ms expiration (already expired)
            JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1);
            String token = expiredProvider.generateToken("user-123", "alice", "student");

            assertThrows(Exception.class, () -> jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("throws for token signed with different key")
        void throwsForDifferentKey() {
            JwtTokenProvider otherProvider = new JwtTokenProvider(
                    "different-secret-key-that-is-also-at-least-256-bits-long-for-hs256",
                    EXPIRATION_MS
            );
            String token = otherProvider.generateToken("user-123", "alice", "student");

            assertThrows(Exception.class, () -> jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("throws for empty token")
        void throwsForEmptyToken() {
            assertThrows(Exception.class, () -> jwtTokenProvider.validateToken(""));
        }

        @Test
        @DisplayName("throws for null token")
        void throwsForNullToken() {
            assertThrows(Exception.class, () -> jwtTokenProvider.validateToken(null));
        }
    }

    // ===== getUserIdFromToken() =====

    @Nested
    @DisplayName("getUserIdFromToken()")
    class GetUserIdFromToken {

        @Test
        @DisplayName("returns userId from valid token")
        void returnsUserIdFromValidToken() {
            String userId = UUID.randomUUID().toString();
            String token = jwtTokenProvider.generateToken(userId, "alice", "student");

            String result = jwtTokenProvider.getUserIdFromToken(token);

            assertEquals(userId, result);
        }

        @Test
        @DisplayName("throws when token is invalid")
        void throwsWhenTokenInvalid() {
            assertThrows(Exception.class, () -> jwtTokenProvider.getUserIdFromToken("invalid.token.here"));
        }
    }
}