package com.yomu.core.service;

import com.yomu.core.dto.ClanDTO;
import com.yomu.core.entity.Clan;
import com.yomu.core.entity.ClanMember;
import com.yomu.core.repository.ClanMemberRepository;
import com.yomu.core.repository.ClanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ClanService {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository clanMemberRepository;

    public ClanService(ClanRepository clanRepository, ClanMemberRepository clanMemberRepository) {
        this.clanRepository = clanRepository;
        this.clanMemberRepository = clanMemberRepository;
    }

    @Transactional
    public ClanDTO createClan(UUID userId, String name) {
        String clanName = normalizeName(name);
        if (clanMemberRepository.existsByUserId(userId)) {
            throw new RuntimeException("You are already in a clan");
        }
        if (clanRepository.existsByName(clanName)) {
            throw new RuntimeException("Clan name already exists");
        }

        Clan clan = new Clan();
        clan.setName(clanName);
        clan.setTier("bronze");
        clan.setTotalScore(BigDecimal.ZERO);
        clan.setLeaderId(userId);
        clan = clanRepository.save(clan);

        ClanMember leader = new ClanMember();
        leader.setClanId(clan.getId());
        leader.setUserId(userId);
        leader.setRole("leader");
        clanMemberRepository.save(leader);

        return toDTO(clan, "leader");
    }

    @Transactional
    public ClanDTO joinClan(UUID userId, UUID clanId) {
        if (clanMemberRepository.existsByUserId(userId)) {
            throw new RuntimeException("You are already in a clan");
        }

        Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new RuntimeException("Clan not found"));

        ClanMember member = new ClanMember();
        member.setClanId(clanId);
        member.setUserId(userId);
        member.setRole("member");
        clanMemberRepository.save(member);

        return toDTO(clan, "member");
    }

    @Transactional
    public void leaveClan(UUID userId, UUID clanId) {
        ClanMember member = clanMemberRepository.findByClanIdAndUserId(clanId, userId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this clan"));
        if ("leader".equals(member.getRole())) {
            throw new RuntimeException("Clan leaders must delete the clan instead of leaving it");
        }
        clanMemberRepository.delete(member);
    }

    @Transactional
    public void deleteClan(UUID requesterId, UUID clanId, boolean admin) {
        Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new RuntimeException("Clan not found"));
        if (!admin && !requesterId.equals(clan.getLeaderId())) {
            throw new RuntimeException("Only the clan leader can delete this clan");
        }

        clanMemberRepository.deleteByClanId(clanId);
        clanRepository.delete(clan);
    }

    private String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new RuntimeException("Clan name is required");
        }
        String clanName = name.trim();
        if (clanName.length() > 100) {
            throw new RuntimeException("Clan name must be at most 100 characters");
        }
        return clanName;
    }

    private ClanDTO toDTO(Clan clan, String myRole) {
        return new ClanDTO(
                clan.getId(),
                clan.getName(),
                clan.getTier(),
                clan.getTotalScore(),
                clan.getLeaderId(),
                clanMemberRepository.findByClanId(clan.getId()).size(),
                myRole
        );
    }
}
