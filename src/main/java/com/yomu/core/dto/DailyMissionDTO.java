package com.yomu.core.dto;

import java.util.UUID;

public class DailyMissionDTO {
    private UUID id;
    private String title;
    private String description;
    private String targetType;
    private Integer targetCount;
    private Integer xpReward;
    private Boolean isActive;

    public DailyMissionDTO(UUID id, String title, String description, String targetType,
                          Integer targetCount, Integer xpReward, Boolean isActive) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.targetType = targetType;
        this.targetCount = targetCount;
        this.xpReward = xpReward;
        this.isActive = isActive;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTargetType() { return targetType; }
    public Integer getTargetCount() { return targetCount; }
    public Integer getXpReward() { return xpReward; }
    public Boolean getIsActive() { return isActive; }
}