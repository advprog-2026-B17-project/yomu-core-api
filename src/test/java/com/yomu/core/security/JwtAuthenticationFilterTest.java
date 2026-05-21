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
        when(request.getHeader(anyString())).thenReturn(null);
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

    @Nested
    @DisplayName("Filter chain continuation")
    class FilterChainContinuation {

        @Test
        @DisplayName("always continues filter chain regardless of authentication state")
        void continuesChainWhenNoAuth() throws Exception {
            HttpServletRequest request = mockRequest("/api/users/me");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("continues filter chain after setting authentication")
        void continuesChainAfterAuth() throws Exception {
            HttpServletRequest request = mockRequest("/api/users/me",
                    "X-User-Id", "user-123");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }
}