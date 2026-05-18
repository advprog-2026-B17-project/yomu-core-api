package com.yomu.core.service;

import com.yomu.core.dto.AuthResponse;
import com.yomu.core.dto.LoginRequest;
import com.yomu.core.dto.RegisterRequest;
import com.yomu.core.entity.User;
import com.yomu.core.repository.UserRepository;
import com.yomu.core.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
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
        User user = userRepository.findByUsername(request.getUsername())
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
    public AuthResponse googleAuth(com.yomu.core.dto.GoogleAuthRequest request) {
        // Find existing user by email or googleId, or create new one
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(request.getUsername());
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
}
