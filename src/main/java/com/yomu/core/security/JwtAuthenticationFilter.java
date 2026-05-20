package com.yomu.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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

        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        String username = request.getHeader("X-Username");

        if (StringUtils.hasText(userId)) {
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + (role != null ? role.toUpperCase() : "STUDENT"))
            );
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
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
