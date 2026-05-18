package com.yomu.core.dto;

import java.util.UUID;

public class AchievementDTO {
    private UUID id;
    private String name;
    private String description;
    private Integer milestone;
    private String iconUrl;
    private boolean unlocked;
    private String unlockedAt;
    private boolean visible;

    public AchievementDTO(UUID id, String name, String description, Integer milestone,
                          String iconUrl, boolean unlocked, String unlockedAt, boolean visible) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.milestone = milestone;
        this.iconUrl = iconUrl;
        this.unlocked = unlocked;
        this.unlockedAt = unlockedAt;
        this.visible = visible;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getMilestone() { return milestone; }
    public String getIconUrl() { return iconUrl; }
    public boolean isUnlocked() { return unlocked; }
    public String getUnlockedAt() { return unlockedAt; }
    public boolean isVisible() { return visible; }
}