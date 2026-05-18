package com.yomu.core.dto;

import java.util.UUID;

public class ClanLeaderboardDTO {
    private UUID clanId;
    private String clanName;
    private String tier;
    private double totalScore;
    private int memberCount;
    private double multiplier;
    private double effectiveScore;

    public ClanLeaderboardDTO(UUID clanId, String clanName, String tier, double totalScore, int memberCount, double multiplier) {
        this.clanId = clanId;
        this.clanName = clanName;
        this.tier = tier;
        this.totalScore = totalScore;
        this.memberCount = memberCount;
        this.multiplier = multiplier;
        this.effectiveScore = totalScore * multiplier;
    }

    public UUID getClanId() { return clanId; }
    public String getClanName() { return clanName; }
    public String getTier() { return tier; }
    public double getTotalScore() { return totalScore; }
    public int getMemberCount() { return memberCount; }
    public double getMultiplier() { return multiplier; }
    public double getEffectiveScore() { return effectiveScore; }
}