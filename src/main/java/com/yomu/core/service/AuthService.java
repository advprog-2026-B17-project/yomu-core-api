package com.yomu.core.service;

import com.yomu.core.dto.AuthResponse;
import com.yomu.core.dto.GoogleAuthRequest;
import com.yomu.core.dto.LoginRequest;
import com.yomu.core.dto.RegisterRequest;
import com.yomu.core.entity.User;
import com.yomu.core.repository.UserRepository;
import com.yomu.core.security.GoogleTokenValidator;
import com.yomu.core.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenValidator googleTokenValidator;
    private final EventPublisher eventPublisher;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       GoogleTokenValidator googleTokenValidator,
                       EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleTokenValidator = googleTokenValidator;
        this.eventPublisher = eventPublisher;
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

        eventPublisher.publishUserCreated(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());

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
        googleTokenValidator.validate(request);

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
                    User savedUser = userRepository.save(newUser);
                    eventPublisher.publishUserCreated(savedUser.getId(), savedUser.getUsername(), savedUser.getDisplayName(), savedUser.getRole());
                    return savedUser;
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
