package com.yomu.core.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AchievementType {
    READING_COUNT("reading_count"),
    QUIZ_PERFECT("quiz_perfect");

    private final String value;

    AchievementType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AchievementType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (AchievementType type : values()) {
            if (type.value.equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported achievement type: " + value);
    }
}
