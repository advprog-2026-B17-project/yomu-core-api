package com.yomu.core.service;

import com.yomu.core.dto.*;
import com.yomu.core.entity.*;
import com.yomu.core.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompletedReadingRepository completedReadingRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserDeletionService userDeletionService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CompletedReadingRepository completedReadingRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       UserDeletionService userDeletionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.completedReadingRepository = completedReadingRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.userDeletionService = userDeletionService;
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<UserDTO> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::toDTO);
    }

    @Transactional
    public Optional<UserDTO> updateUser(UUID id, UpdateUserRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
                        userRepository.findByUsername(request.getUsername()).ifPresent(existing -> {
                            throw new RuntimeException("Username already taken");
                        });
                        user.setUsername(request.getUsername());
                    }

                    if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
                        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                            throw new RuntimeException("Email already registered");
                        });
                        user.setEmail(request.getEmail());
                    }

                    if (request.getPhone() != null) {
                        String normalizedPhone = request.getPhone().trim();
                        if (normalizedPhone.isBlank()) {
                            user.setPhone(null);
                        } else if (!normalizedPhone.equals(user.getPhone())) {
                            userRepository.findByPhone(normalizedPhone).ifPresent(existing -> {
                                throw new RuntimeException("Phone already registered");
                            });
                            user.setPhone(normalizedPhone);
                        }
                    }

                    if (StringUtils.hasText(request.getDisplayName())) {
                        user.setDisplayName(request.getDisplayName());
                    }
                    user.setUpdatedAt(OffsetDateTime.now());

                    if (request.isUpdatePassword() && request.getPassword() != null) {
                        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                    }

                    User updated = userRepository.save(user);
                    return toDTO(updated);
                });
    }

    @Transactional
    public boolean deleteUser(UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    userDeletionService.cascadeDelete(id);
                    userRepository.delete(user);
                    return true;
                })
                .orElse(false);
    }

    public UserProfileDTO getUserProfile(UUID userId) {
        // Get user info
        Optional<User> userOpt = userRepository.findById(userId);
        Map<String, Object> userMap = userOpt.map(u -> Map.<String, Object>of(
            "id", u.getId().toString(),
            "username", u.getUsername(),
            "email", u.getEmail(),
            "displayName", u.getDisplayName(),
            "role", u.getRole()
        )).orElse(Map.of());

        // Get stats
        long readings = completedReadingRepository.countByUserId(userId);
        long quizzes = quizAttemptRepository.countByUserId(userId);
        Double accuracy = quizAttemptRepository.getAverageAccuracyByUserId(userId);
        StatsDTO stats = new StatsDTO(readings, quizzes, accuracy != null ? accuracy : 0.0);

        // Achievements and clan are gamification concerns - handled by yomu-gamification-engine
        return new UserProfileDTO(userMap, stats);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getDisplayName(),
                user.getRole()
        );
    }
}