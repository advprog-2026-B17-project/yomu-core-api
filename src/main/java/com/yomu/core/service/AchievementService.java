package com.yomu.core.service;

import com.yomu.core.dto.AchievementDTO;
import com.yomu.core.dto.CreateAchievementRequest;
import com.yomu.core.entity.Achievement;
import com.yomu.core.entity.UserAchievement;
import com.yomu.core.repository.AchievementRepository;
import com.yomu.core.repository.UserAchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    public AchievementService(AchievementRepository achievementRepository,
                              UserAchievementRepository userAchievementRepository) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    public List<AchievementDTO> getAllAchievements() {
        return achievementRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public AchievementDTO createAchievement(CreateAchievementRequest request) {
        Achievement achievement = new Achievement();
        applyRequest(achievement, request);
        return toDTO(achievementRepository.save(achievement));
    }

    @Transactional
    public AchievementDTO updateAchievement(UUID id, CreateAchievementRequest request) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Achievement not found"));
        applyRequest(achievement, request);
        return toDTO(achievementRepository.save(achievement));
    }

    @Transactional
    public void deleteAchievement(UUID id) {
        achievementRepository.deleteById(id);
    }

    @Transactional
    public Optional<UserAchievement> setVisibility(UUID userId, UUID achievementId, boolean visible) {
        return userAchievementRepository.findByUserIdAndAchievementId(userId, achievementId)
                .map(userAchievement -> {
                    userAchievement.setIsVisible(visible);
                    return userAchievementRepository.save(userAchievement);
                });
    }

    private void applyRequest(Achievement achievement, CreateAchievementRequest request) {
        achievement.setName(request.getName());
        achievement.setDescription(request.getDescription());
        achievement.setMilestone(request.getMilestone());
        achievement.setAchievementType(
                StringUtils.hasText(request.getAchievementType()) ? request.getAchievementType() : "reading_count"
        );
        achievement.setIconUrl(request.getIconUrl());
    }

    private AchievementDTO toDTO(Achievement achievement) {
        return new AchievementDTO(
                achievement.getId(),
                achievement.getName(),
                achievement.getDescription(),
                achievement.getMilestone(),
                achievement.getAchievementType(),
                achievement.getIconUrl()
        );
    }
}
