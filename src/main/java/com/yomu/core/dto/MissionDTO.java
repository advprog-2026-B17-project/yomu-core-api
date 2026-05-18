package com.yomu.core.dto;

import java.time.LocalDate;
import java.util.UUID;

public class MissionDTO {
    private UUID id;
    private String title;
    private String description;
    private String targetType;
    private Integer targetCount;
    private Integer xpReward;
    private Integer progress;
    private Boolean claimed;
    private LocalDate date;

    public MissionDTO() {}

    public MissionDTO(UUID id, String title, String description, String targetType,
                      Integer targetCount, Integer xpReward, Integer progress,
                      Boolean claimed, LocalDate date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.targetType = targetType;
        this.targetCount = targetCount;
        this.xpReward = xpReward;
        this.progress = progress;
        this.claimed = claimed;
        this.date = date;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Integer getTargetCount() { return targetCount; }
    public void setTargetCount(Integer targetCount) { this.targetCount = targetCount; }
    public Integer getXpReward() { return xpReward; }
    public void setXpReward(Integer xpReward) { this.xpReward = xpReward; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public Boolean getClaimed() { return claimed; }
    public void setClaimed(Boolean claimed) { this.claimed = claimed; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}