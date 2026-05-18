package com.yomu.core.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public class CreateSeasonRequest {
    @NotBlank(message = "Season name is required")
    private String name;

    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public OffsetDateTime getStartDate() { return startDate; }
    public void setStartDate(OffsetDateTime startDate) { this.startDate = startDate; }
    public OffsetDateTime getEndDate() { return endDate; }
    public void setEndDate(OffsetDateTime endDate) { this.endDate = endDate; }
}