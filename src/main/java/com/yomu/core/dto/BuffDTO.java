package com.yomu.core.dto;

import java.time.OffsetDateTime;

public class BuffDTO {
    private String buffType;
    private Double multiplier;
    private OffsetDateTime activatedAt;
    private String description;

    public BuffDTO() {}

    public BuffDTO(String buffType, Double multiplier, OffsetDateTime activatedAt, String description) {
        this.buffType = buffType;
        this.multiplier = multiplier;
        this.activatedAt = activatedAt;
        this.description = description;
    }

    public String getBuffType() { return buffType; }
    public void setBuffType(String buffType) { this.buffType = buffType; }
    public Double getMultiplier() { return multiplier; }
    public void setMultiplier(Double multiplier) { this.multiplier = multiplier; }
    public OffsetDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(OffsetDateTime activatedAt) { this.activatedAt = activatedAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
