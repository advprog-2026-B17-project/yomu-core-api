package com.yomu.core.service;

import com.yomu.core.dto.ClanDTO;
import com.yomu.core.entity.Clan;
import com.yomu.core.entity.ClanMember;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClanService {

    private final com.yomu.core.repository.ClanRepository clanRepository;
    private final com.yomu.core.repository.ClanMemberRepository clanMemberRepository;
    private final com.yomu.core.repository.UserRepository userRepository;
    private final ClanNotificationService clanNotificationService;
    private final com.yomu.core.strategy.ClanScoringContext clanScoringContext;

    public ClanService(
            com.yomu.core.repository.ClanRepository clanRepository,
            com.yomu.core.repository.ClanMemberRepository clanMemberRepository,
            com.yomu.core.repository.UserRepository userRepository,
            ClanNotificationService clanNotificationService,
            com.yomu.core.strategy.ClanScoringContext clanScoringContext) {
        this.clanRepository = clanRepository;
        this.clanMemberRepository = clanMemberRepository;
        this.userRepository = userRepository;
        this.clanNotificationService = clanNotificationService;
        this.clanScoringContext = clanScoringContext;
    }

    private ClanDTO toClanDTO(Clan clan) {
        long memberCount = clanMemberRepository.findByClanId(clan.getId()).size();
        String myRole = clanMemberRepository.findByClanId(clan.getId()).stream()
                .findFirst().map(ClanMember::getRole).orElse(null);
        return new ClanDTO(clan.getId(), clan.getName(), clan.getTier(),
                clan.getTotalScore(), clan.getLeaderId(), memberCount, myRole);
    }

    @Transactional
    public ClanDTO createClan(UUID leaderId, String name) {
        if (clanRepository.existsByName(name)) {
            throw new RuntimeException("Clan name already exists");
        }

        Clan clan = new Clan();
        clan.setName(name);
        clan.setLeaderId(leaderId);
        clan.setTier("bronze");
        clan.setTotalScore(BigDecimal.ZERO);
        clan = clanRepository.save(clan);

        ClanMember member = new ClanMember();
        member.setClanId(clan.getId());
        member.setUserId(leaderId);
        member.setRole("leader");
        clanMemberRepository.save(member);

        return toClanDTO(clan);
    }

    @Transactional
    public ClanDTO joinClan(UUID userId, UUID clanId) {
        if (clanMemberRepository.existsByClanIdAndUserId(clanId, userId)) {
            throw new RuntimeException("Already a member of this clan");
        }

        Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new RuntimeException("Clan not found"));

        ClanMember member = new ClanMember();
        member.setClanId(clanId);
        member.setUserId(userId);
        member.setRole("member");
        clanMemberRepository.save(member);

        String joinerName = userRepository.findById(userId)
                .map(u -> u.getDisplayName()).orElse("Seseorang");
        clanNotificationService.notifyClanMembers(clanId, userId, "clan_event",
                "Anggota Baru di " + clan.getName(),
                joinerName + " bergabung dengan clan kamu!");

        return toClanDTO(clan);
    }

    @Transactional
    public void leaveClan(UUID userId, UUID clanId) {
        Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new RuntimeException("Clan not found"));

        if (clan.getLeaderId().equals(userId)) {
            throw new RuntimeException("Leader cannot leave the clan. Transfer leadership first or delete the clan.");
        }

        String leaverName = userRepository.findById(userId)
                .map(u -> u.getDisplayName()).orElse("Seseorang");
        clanMemberRepository.deleteByClanIdAndUserId(clanId, userId);

        clanNotificationService.notifyClanMembers(clanId, userId, "clan_event",
                "Anggota Keluar dari " + clan.getName(),
                leaverName + " telah keluar dari clan.");
    }

    @Transactional
    public void deleteClan(UUID requestingUserId, UUID clanId, boolean isAdmin) {
        Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new RuntimeException("Clan not found"));

        if (!isAdmin && !clan.getLeaderId().equals(requestingUserId)) {
            throw new RuntimeException("Only the clan leader can delete the clan");
        }

        clanNotificationService.notifyClanMembers(clanId, requestingUserId, "clan_event",
                "Clan " + clan.getName() + " Dibubarkan",
                isAdmin ? "Clan kamu telah dibubarkan oleh admin."
                        : "Clan kamu telah dibubarkan oleh pemimpin.");

        clanMemberRepository.deleteByClanId(clanId);
        clanRepository.delete(clan);
    }

    public Optional<ClanDTO> getClanById(UUID clanId) {
        return clanRepository.findById(clanId).map(this::toClanDTO);
    }

    public List<ClanDTO> getAllClans() {
        return clanRepository.findAllOrderByTotalScoreDesc().stream()
                .map(this::toClanDTO)
                .collect(Collectors.toList());
    }

    public List<ClanDTO> getLeaderboard() {
        return clanRepository.findAllOrderByTotalScoreDesc().stream()
                .map(clan -> {
                    int memberCount = (int) clanMemberRepository.findByClanId(clan.getId()).size();
                    double effectiveScore = clanScoringContext.calculateScore(
                            clan.getTier(),
                            clan.getTotalScore().doubleValue(),
                            memberCount
                    );
                    return new com.yomu.core.dto.ClanDTO(
                            clan.getId(), clan.getName(), clan.getTier(),
                            java.math.BigDecimal.valueOf(effectiveScore),
                            clan.getLeaderId(), memberCount, null
                    );
                })
                .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
                .collect(Collectors.toList());
    }

    public List<ClanDTO> getLeaderboardByTier(String tier) {
        return clanRepository.findByTierOrderByTotalScoreDesc(tier.toLowerCase()).stream()
                .map(clan -> {
                    int memberCount = (int) clanMemberRepository.findByClanId(clan.getId()).size();
                    double effectiveScore = clanScoringContext.calculateScore(
                            clan.getTier(),
                            clan.getTotalScore().doubleValue(),
                            memberCount
                    );
                    return new com.yomu.core.dto.ClanDTO(
                            clan.getId(), clan.getName(), clan.getTier(),
                            java.math.BigDecimal.valueOf(effectiveScore),
                            clan.getLeaderId(), memberCount, null
                    );
                })
                .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
                .collect(Collectors.toList());
    }
    public Optional<ClanDTO> getMyClan(UUID userId) {
        return clanMemberRepository.findByUserId(userId).stream()
                .findFirst()
                .flatMap(m -> clanRepository.findById(m.getClanId()))
                .map(this::toClanDTO);
    }
}