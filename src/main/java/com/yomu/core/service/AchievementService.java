package com.yomu.core.service;

import com.yomu.core.dto.AchievementDTO;
import com.yomu.core.entity.Achievement;
import com.yomu.core.entity.UserAchievement;
import com.yomu.core.repository.AchievementRepository;
import com.yomu.core.repository.UserAchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    public AchievementService(AchievementRepository achievementRepository,
                             UserAchievementRepository userAchievementRepository) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    public List<AchievementDTO> getAllAchievementsWithStatus(UUID userId) {
        List<Achievement> allAchievements = achievementRepository.findAll();
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);

        return allAchievements.stream().map(a -> {
            UserAchievement ua = userAchievements.stream()
                    .filter(u -> u.getAchievementId().equals(a.getId()))
                    .findFirst()
                    .orElse(null);

            return new AchievementDTO(
                    a.getId(),
                    a.getName(),
                    a.getDescription(),
                    a.getMilestone(),
                    a.getIconUrl(),
                    ua != null,
                    ua != null ? ua.getUnlockedAt().toString() : null,
                    ua != null && ua.getIsVisible()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void toggleAchievementVisibility(UUID userId, UUID achievementId, boolean visible) {
        UserAchievement ua = userAchievementRepository.findByUserIdAndAchievementId(userId, achievementId)
                .orElseThrow(() -> new RuntimeException("Achievement not unlocked"));

        ua.setIsVisible(visible);
        userAchievementRepository.save(ua);
    }

    public List<AchievementDTO> getVisibleAchievements(UUID userId) {
        return userAchievementRepository.findByUserIdAndIsVisibleTrue(userId).stream()
                .map(ua -> {
                    Achievement a = achievementRepository.findById(ua.getAchievementId()).orElse(null);
                    if (a == null) return null;
                    return new AchievementDTO(
                            a.getId(), a.getName(), a.getDescription(), a.getMilestone(),
                            a.getIconUrl(), true, ua.getUnlockedAt().toString(), true
                    );
                })
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }
}