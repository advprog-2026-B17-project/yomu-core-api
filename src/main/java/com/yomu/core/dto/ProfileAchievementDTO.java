package com.yomu.core.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ProfileAchievementDTO {
    private final UUID id;
    private final String name;
    private final String description;
    private final Integer milestone;
    private final String achievementType;
    private final String iconUrl;
    private final OffsetDateTime unlockedAt;
    private final boolean visible;

    public ProfileAchievementDTO(UUID id, String name, String description, Integer milestone,
            String achievementType, String iconUrl, OffsetDateTime unlockedAt, boolean visible) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.milestone = milestone;
        this.achievementType = achievementType;
        this.iconUrl = iconUrl;
        this.unlockedAt = unlockedAt;
        this.visible = visible;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getMilestone() {
        return milestone;
    }

    public String getAchievementType() {
        return achievementType;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public OffsetDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public boolean isVisible() {
        return visible;
    }
}
