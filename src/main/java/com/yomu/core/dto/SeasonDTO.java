package com.yomu.core.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SeasonDTO {
    private UUID id;
    private String name;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private boolean isActive;

    public SeasonDTO() {}

    public SeasonDTO(UUID id, String name, OffsetDateTime startDate, OffsetDateTime endDate, boolean isActive) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public OffsetDateTime getStartDate() { return startDate; }
    public void setStartDate(OffsetDateTime startDate) { this.startDate = startDate; }
    public OffsetDateTime getEndDate() { return endDate; }
    public void setEndDate(OffsetDateTime endDate) { this.endDate = endDate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}