package com.yomu.core.dto;

import java.util.List;
import java.util.UUID;

public class ClanBuffsResponse {
    private UUID clanId;
    private List<BuffDTO> buffs;

    public ClanBuffsResponse() {}

    public ClanBuffsResponse(UUID clanId, List<BuffDTO> buffs) {
        this.clanId = clanId;
        this.buffs = buffs;
    }

    public UUID getClanId() { return clanId; }
    public void setClanId(UUID clanId) { this.clanId = clanId; }
    public List<BuffDTO> getBuffs() { return buffs; }
    public void setBuffs(List<BuffDTO> buffs) { this.buffs = buffs; }
}
