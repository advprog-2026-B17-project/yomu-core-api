package com.yomu.core.dto;

import java.util.UUID;

public class ClanDTO {
    private UUID id;
    private String name;
    private String tier;
    private double totalScore;
    private UUID leaderId;
    private String leaderName;
    private int memberCount;
    private String currentTier;
    private String previewTier;
    private Boolean willPromote;
    private Boolean willDemote;

    public ClanDTO(UUID id, String name, String tier, double totalScore, UUID leaderId, String leaderName, int memberCount) {
        this.id = id;
        this.name = name;
        this.tier = tier;
        this.totalScore = totalScore;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.memberCount = memberCount;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getTier() { return tier; }
    public double getTotalScore() { return totalScore; }
    public UUID getLeaderId() { return leaderId; }
    public String getLeaderName() { return leaderName; }
    public int getMemberCount() { return memberCount; }
    public String getCurrentTier() { return currentTier; }
    public void setCurrentTier(String currentTier) { this.currentTier = currentTier; }
    public String getPreviewTier() { return previewTier; }
    public void setPreviewTier(String previewTier) { this.previewTier = previewTier; }
    public Boolean getWillPromote() { return willPromote; }
    public void setWillPromote(Boolean willPromote) { this.willPromote = willPromote; }
    public Boolean getWillDemote() { return willDemote; }
    public void setWillDemote(Boolean willDemote) { this.willDemote = willDemote; }
}