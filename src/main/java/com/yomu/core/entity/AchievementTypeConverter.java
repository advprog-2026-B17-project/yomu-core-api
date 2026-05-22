package com.yomu.core.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AchievementTypeConverter implements AttributeConverter<AchievementType, String> {

    @Override
    public String convertToDatabaseColumn(AchievementType attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public AchievementType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return AchievementType.READING_COUNT;
        }

        return AchievementType.fromValue(dbData);
    }
}
