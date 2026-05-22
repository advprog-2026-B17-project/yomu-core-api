package com.yomu.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class GatewaySecretFilter extends OncePerRequestFilter {

    @Value("${GATEWAY_SHARED_SECRET:}")
    private String gatewaySharedSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (requiresGatewaySecret(request) && !hasValidGatewaySecret(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean requiresGatewaySecret(HttpServletRequest request) {
        String path = request.getRequestURI();
        return StringUtils.hasText(gatewaySharedSecret)
                && path.startsWith("/api/")
                && !path.startsWith("/api/auth/");
    }

    private boolean hasValidGatewaySecret(HttpServletRequest request) {
        return gatewaySharedSecret.equals(request.getHeader("X-Gateway-Secret"));
    }
}
