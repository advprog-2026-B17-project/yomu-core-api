package com.yomu.core.dto;

import java.util.List;
import java.util.Map;

public class UserProfileDTO {
    private Map<String, Object> user;
    private StatsDTO stats;

    public UserProfileDTO(Map<String, Object> user, StatsDTO stats) {
        this.user = user;
        this.stats = stats;
    }

    public Map<String, Object> getUser() { return user; }
    public StatsDTO getStats() { return stats; }
}