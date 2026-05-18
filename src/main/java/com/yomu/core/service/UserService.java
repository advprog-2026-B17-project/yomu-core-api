package com.yomu.core.service;

import com.yomu.core.dto.*;
import com.yomu.core.entity.*;
import com.yomu.core.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompletedReadingRepository completedReadingRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRepository achievementRepository;
    private final ClanMemberRepository clanMemberRepository;
    private final ClanRepository clanRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CompletedReadingRepository completedReadingRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       UserAchievementRepository userAchievementRepository,
                       AchievementRepository achievementRepository,
                       ClanMemberRepository clanMemberRepository,
                       ClanRepository clanRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.completedReadingRepository = completedReadingRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.achievementRepository = achievementRepository;
        this.clanMemberRepository = clanMemberRepository;
        this.clanRepository = clanRepository;
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
                    user.setDisplayName(request.getDisplayName());
                    user.setUpdatedAt(OffsetDateTime.now());

                    if (request.isUpdatePassword() && request.getPassword() != null) {
                        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                    }

                    User updated = userRepository.save(user);
                    return toDTO(updated);
                });
    }

    public UserProfileDTO getUserProfile(UUID userId) {
        // Get user info
        Optional<User> userOpt = userRepository.findById(userId);
        Map<String, Object> userMap = userOpt.map(u -> Map.<String, Object>of(
            "id", u.getId().toString(),
            "username", u.getUsername(),
            "displayName", u.getDisplayName(),
            "role", u.getRole()
        )).orElse(Map.of());

        // Get stats
        long readings = completedReadingRepository.countByUserId(userId);
        long quizzes = quizAttemptRepository.countByUserId(userId);
        Double accuracy = quizAttemptRepository.getAverageAccuracyByUserId(userId);
        StatsDTO stats = new StatsDTO(readings, quizzes, accuracy != null ? accuracy : 0.0);

        // Get achievements (all unlocked)
        List<UserAchievement> uaList = userAchievementRepository.findByUserId(userId);
        List<Map<String, Object>> achievements = uaList.stream().map(ua -> {
            Optional<Achievement> achOpt = achievementRepository.findById(ua.getAchievementId());
            return achOpt.map(ach -> Map.<String, Object>of(
                "id", ach.getId().toString(),
                "name", ach.getName(),
                "unlockedAt", ua.getUnlockedAt().toString(),
                "visible", ua.getIsVisible() != null ? ua.getIsVisible() : true
            )).orElse(null);
        }).filter(Objects::nonNull).collect(Collectors.toList());

        // Get clan
        List<ClanMember> memberships = clanMemberRepository.findByUserId(userId);
        Map<String, Object> clan = null;
        if (!memberships.isEmpty()) {
            UUID clanId = memberships.get(0).getClanId();
            Optional<Clan> clanOpt = clanRepository.findById(clanId);
            clan = clanOpt.map(c -> Map.<String, Object>of(
                "id", c.getId().toString(),
                "name", c.getName(),
                "tier", c.getTier(),
                "role", memberships.get(0).getRole()
            )).orElse(null);
        }

        return new UserProfileDTO(userMap, stats, achievements, clan);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }
}
