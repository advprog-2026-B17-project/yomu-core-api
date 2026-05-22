package com.yomu.core.repository;

import com.yomu.core.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    List<UserAchievement> findByUserId(UUID userId);
    List<UserAchievement> findByUserIdAndIsVisibleTrue(UUID userId);
    List<UserAchievement> findByUserIdOrderByUnlockedAtDesc(UUID userId);
    List<UserAchievement> findByUserIdAndIsVisibleTrueOrderByUnlockedAtDesc(UUID userId);
    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId);
}
