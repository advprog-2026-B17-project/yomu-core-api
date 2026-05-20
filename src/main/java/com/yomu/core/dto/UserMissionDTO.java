package com.yomu.core.dto;

import java.time.LocalDate;
import java.util.UUID;

public class UserMissionDTO {
    private UUID id;
    private UUID missionId;
    private Integer progress;
    private Integer targetCount;
    private Integer xpReward;
    private Boolean claimed;
    private LocalDate date;

    public UserMissionDTO(UUID id, UUID missionId, Integer progress, Integer targetCount,
                          Integer xpReward, Boolean claimed, LocalDate date) {
        this.id = id;
        this.missionId = missionId;
        this.progress = progress;
        this.targetCount = targetCount;
        this.xpReward = xpReward;
        this.claimed = claimed;
        this.date = date;
    }

    public UUID getId() { return id; }
    public UUID getMissionId() { return missionId; }
    public Integer getProgress() { return progress; }
    public Integer getTargetCount() { return targetCount; }
    public Integer getXpReward() { return xpReward; }
    public Boolean getClaimed() { return claimed; }
    public LocalDate getDate() { return date; }
}
