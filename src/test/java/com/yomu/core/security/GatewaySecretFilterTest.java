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
class GatewaySecretFilterTest {

    @Mock
    private FilterChain filterChain;

    private GatewaySecretFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GatewaySecretFilter();
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
    @DisplayName("Gateway secret validation")
    class GatewaySecretValidation {

        @Test
        @DisplayName("allows request without gateway secret when GATEWAY_SHARED_SECRET not set")
        void allowsWithoutSecretWhenNotConfigured() throws Exception {
            HttpServletRequest request = mockRequest("/api/users/me");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
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
                    "X-Gateway-Secret", "secret123");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
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

        @Test
        @DisplayName("allows /api/auth/register without secret even when secret is configured")
        void allowsAuthRegisterWithoutSecret() throws Exception {
            setFieldValue(filter, "gatewaySharedSecret", "secret123");

            HttpServletRequest request = mockRequest("/api/auth/register");
            HttpServletResponse response = mockResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }
    }

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