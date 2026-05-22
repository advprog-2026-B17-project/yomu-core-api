package com.yomu.core.entity;

public enum AchievementType {
    READING_COUNT("reading_count"),
    QUIZ_PERFECT("quiz_perfect");

    private final String value;

    AchievementType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AchievementType fromValue(String value) {
        for (AchievementType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported achievement type: " + value);
    }
}