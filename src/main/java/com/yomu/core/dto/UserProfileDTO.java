package com.yomu.core.dto;

import java.util.List;
import java.util.Map;

public class UserProfileDTO {
    private Map<String, Object> user;
    private StatsDTO stats;
    private List<Map<String, Object>> achievements;
    private Map<String, Object> clan;

    public UserProfileDTO(Map<String, Object> user, StatsDTO stats,
                        List<Map<String, Object>> achievements, Map<String, Object> clan) {
        this.user = user;
        this.stats = stats;
        this.achievements = achievements;
        this.clan = clan;
    }

    public Map<String, Object> getUser() { return user; }
    public StatsDTO getStats() { return stats; }
    public List<Map<String, Object>> getAchievements() { return achievements; }
    public Map<String, Object> getClan() { return clan; }
}