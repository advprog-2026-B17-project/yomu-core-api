package com.yomu.core.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yomu.core.dto.AuthResponse;
import com.yomu.core.dto.GoogleAuthRequest;
import com.yomu.core.dto.LoginRequest;
import com.yomu.core.dto.RegisterRequest;
import com.yomu.core.entity.User;
import com.yomu.core.repository.UserRepository;
import com.yomu.core.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String googleClientId;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       ObjectMapper objectMapper,
                       @Value("${GOOGLE_CLIENT_ID:}") String googleClientId) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
        this.googleClientId = googleClientId;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        user.setDisplayName(request.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("student");

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(
                user.getId().toString(),
                user.getUsername(),
                user.getRole()
        );

        return new AuthResponse(token, user.getId().toString(), user.getUsername(),
                user.getDisplayName(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsername();
        User user = userRepository.findFirstByUsernameOrEmailOrPhone(identifier, identifier, identifier)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(
                user.getId().toString(),
                user.getUsername(),
                user.getRole()
        );

        return new AuthResponse(token, user.getId().toString(), user.getUsername(),
                user.getDisplayName(), user.getRole());
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(java.util.UUID.fromString(userId));
    }

    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        validateGoogleAuthRequest(request);

        // Find existing user by email or googleId, or create new one
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(uniqueGoogleUsername(request));
                    newUser.setEmail(request.getEmail());
                    newUser.setDisplayName(request.getDisplayName());
                    newUser.setGoogleId(request.getGoogleId());
                    newUser.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString())); // Random password
                    newUser.setRole("student");
                    return userRepository.save(newUser);
                });

        // Update googleId if not set
        if (user.getGoogleId() == null && request.getGoogleId() != null) {
            user.setGoogleId(request.getGoogleId());
            user = userRepository.save(user);
        }

        String token = jwtTokenProvider.generateToken(
                user.getId().toString(),
                user.getUsername(),
                user.getRole()
        );

        return new AuthResponse(token, user.getId().toString(), user.getUsername(),
                user.getDisplayName(), user.getRole());
    }

    private void validateGoogleAuthRequest(GoogleAuthRequest request) {
        if (!StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getGoogleId())) {
            throw new RuntimeException("Google email and subject are required");
        }
        if (!StringUtils.hasText(googleClientId)) {
            return;
        }
        if (!StringUtils.hasText(request.getIdToken())) {
            throw new RuntimeException("Missing Google ID token");
        }

        try {
            String encodedToken = URLEncoder.encode(request.getIdToken(), StandardCharsets.UTF_8);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + encodedToken))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Invalid Google ID token");
            }

            Map<String, Object> tokenInfo = objectMapper.readValue(response.body(), new TypeReference<>() {});
            if (!googleClientId.equals(tokenInfo.get("aud"))) {
                throw new RuntimeException("Google token audience mismatch");
            }
            if (!request.getEmail().equals(tokenInfo.get("email"))) {
                throw new RuntimeException("Google token email mismatch");
            }
            if (StringUtils.hasText(request.getGoogleId()) && !request.getGoogleId().equals(tokenInfo.get("sub"))) {
                throw new RuntimeException("Google token subject mismatch");
            }
            if (!"true".equals(String.valueOf(tokenInfo.get("email_verified")))) {
                throw new RuntimeException("Google email is not verified");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to verify Google ID token");
        }
    }

    private String uniqueGoogleUsername(GoogleAuthRequest request) {
        String base = StringUtils.hasText(request.getUsername())
                ? request.getUsername()
                : request.getEmail().split("@")[0];
        base = base.replaceAll("[^A-Za-z0-9_]", "_");
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }
}
