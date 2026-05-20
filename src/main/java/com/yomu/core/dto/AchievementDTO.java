package com.yomu.core.dto;

import java.util.UUID;

public class AchievementDTO {
    private UUID id;
    private String name;
    private String description;
    private Integer milestone;
    private String achievementType;
    private String iconUrl;

    public AchievementDTO() {}

    public AchievementDTO(UUID id, String name, String description, Integer milestone, String achievementType, String iconUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.milestone = milestone;
        this.achievementType = achievementType;
        this.iconUrl = iconUrl;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMilestone() { return milestone; }
    public void setMilestone(Integer milestone) { this.milestone = milestone; }
    public String getAchievementType() { return achievementType; }
    public void setAchievementType(String achievementType) { this.achievementType = achievementType; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}
