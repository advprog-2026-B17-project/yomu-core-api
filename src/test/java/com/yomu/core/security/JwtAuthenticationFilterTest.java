package com.yomu.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationFilterTest {

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        SecurityContextHolder.clearContext();
    }

    private HttpServletRequest mockRequest(String uri, String... headers) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        // Default: no headers
        when(request.getHeader(anyString())).thenReturn(null);
        // Set headers from varargs
        for (int i = 0; i < headers.length - 1; i += 2) {
            String name = headers[i];
            String value = headers[i + 1];
            when(request.getHeader(name)).thenReturn(value);
        }
        return request;
    }

    private HttpServletResponse mockResponse() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);
        return response;
    }

    // ===== Gateway secret validation =====

    @Nested
    @DisplayName("Gateway secret validation")
    class GatewaySecret {

        @Test
        @DisplayName("allows request without gateway secret when GATEWAY_SHARED_SECRET not set")
        void allowsWithoutSecretWhenNotConfigured() throws Exception {
            HttpServletRequest request = mockRequest("/api/users/me",
                    "X-User-Id", "user-123");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertEquals("user-123", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        }

        @Test
        @DisplayName("blocks request without gateway secret when GATEWAY_SHARED_SECRET is set")
        void blocksWithoutSecretWhenConfigured() throws Exception {
            setFieldValue(filter, "gatewaySharedSecret", "secret123");

            HttpServletRequest request = mockRequest("/api/users/me");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        @Test
        @DisplayName("allows request with valid gateway secret")
        void allowsWithValidSecret() throws Exception {
            setFieldValue(filter, "gatewaySharedSecret", "secret123");

            HttpServletRequest request = mockRequest("/api/users/me",
                    "X-Gateway-Secret", "secret123",
                    "X-User-Id", "user-123");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("blocks request with invalid gateway secret")
        void blocksWithInvalidSecret() throws Exception {
            setFieldValue(filter, "gatewaySharedSecret", "secret123");

            HttpServletRequest request = mockRequest("/api/users/me",
                    "X-Gateway-Secret", "wrongsecret");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        @Test
        @DisplayName("auth paths are excluded from gateway secret check")
        void authPathsExcludedFromSecretCheck() throws Exception {
            setFieldValue(filter, "gatewaySharedSecret", "secret123");

            HttpServletRequest request = mockRequest("/api/auth/login",
                    "X-User-Id", "user-123");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }
    }

    // ===== X-User-Id header handling =====

    @Nested
    @DisplayName("X-User-Id header handling")
    class UserIdHeader {

        @Test
        @DisplayName("sets authentication when X-User-Id is present")
        void setsAuthenticationWhenUserIdPresent() throws Exception {
            HttpServletRequest request = mockRequest("/api/users/me",
                    "X-User-Id", "user-123",
                    "X-User-Role", "student",
                    "X-Username", "alice");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals("user-123", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        }

        @Test
        @DisplayName("sets correct role authority from X-User-Role header")
        void setsCorrectRoleAuthority() throws Exception {
            HttpServletRequest request = mockRequest("/api/users/me",
                    "X-User-Id", "user-123",
                    "X-User-Role", "admin");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertTrue(SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("defaults to ROLE_STUDENT when X-User-Role is null")
        void defaultsToStudentRoleWhenNull() throws Exception {
            HttpServletRequest request = mockRequest("/api/users/me",
                    "X-User-Id", "user-123");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertTrue(SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
        }

        @Test
        @DisplayName("converts role to uppercase when setting authority")
        void convertsRoleToUppercase() throws Exception {
            HttpServletRequest request = mockRequest("/api/users/me",
                    "X-User-Id", "user-123",
                    "X-User-Role", "admin");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertTrue(SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("does not set authentication when X-User-Id is blank")
        void doesNotSetAuthWhenUserIdBlank() throws Exception {
            HttpServletRequest request = mockRequest("/api/users/me",
                    "X-User-Id", "   ");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("does not set authentication when X-User-Id is missing")
        void doesNotSetAuthWhenUserIdMissing() throws Exception {
            HttpServletRequest request = mockRequest("/api/users/me");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
        }
    }

    // ===== Path-based routing =====

    @Nested
    @DisplayName("Path-based routing")
    class PathBasedRouting {

        @Test
        @DisplayName("skips gateway secret check for /api/auth/** paths")
        void skipsSecretCheckForAuthPaths() throws Exception {
            setFieldValue(filter, "gatewaySharedSecret", "secret123");

            HttpServletRequest request = mockRequest("/api/auth/register");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("skips gateway secret check for /actuator/** paths")
        void skipsSecretCheckForActuatorPaths() throws Exception {
            setFieldValue(filter, "gatewaySharedSecret", "secret123");

            HttpServletRequest request = mockRequest("/actuator/health");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
            verify(filterChain).doFilter(request, response);
        }
    }

    // Utility to set private fields via reflection
    private void setFieldValue(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}