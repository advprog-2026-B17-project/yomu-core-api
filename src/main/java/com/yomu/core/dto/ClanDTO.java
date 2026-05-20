package com.yomu.core.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ClanDTO {
    private UUID id;
    private String name;
    private String tier;
    private BigDecimal totalScore;
    private UUID leaderId;
    private long memberCount;
    private String myRole;

    public ClanDTO(UUID id, String name, String tier, BigDecimal totalScore,
                   UUID leaderId, long memberCount, String myRole) {
        this.id = id;
        this.name = name;
        this.tier = tier;
        this.totalScore = totalScore;
        this.leaderId = leaderId;
        this.memberCount = memberCount;
        this.myRole = myRole;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getTier() { return tier; }
    public BigDecimal getTotalScore() { return totalScore; }
    public UUID getLeaderId() { return leaderId; }
    public long getMemberCount() { return memberCount; }
    public String getMyRole() { return myRole; }
}
