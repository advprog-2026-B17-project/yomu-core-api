package com.yomu.core.dto;

import java.util.UUID;

public class AchievementVisibilityDTO {
    private final UUID achievementId;
    private final boolean visible;

    public AchievementVisibilityDTO(UUID achievementId, boolean visible) {
        this.achievementId = achievementId;
        this.visible = visible;
    }

    public UUID getAchievementId() {
        return achievementId;
    }

    public boolean isVisible() {
        return visible;
    }
}
