package com.yomu.core.service;

import com.yomu.core.dto.ClanDTO;
import com.yomu.core.dto.ClanLeaderboardDTO;
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

    public ClanService(
            com.yomu.core.repository.ClanRepository clanRepository,
            com.yomu.core.repository.ClanMemberRepository clanMemberRepository,
            com.yomu.core.repository.UserRepository userRepository) {
        this.clanRepository = clanRepository;
        this.clanMemberRepository = clanMemberRepository;
        this.userRepository = userRepository;
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

        int memberCount = clanMemberRepository.findByClanId(clan.getId()).size();
        String leaderName = userRepository.findById(leaderId).map(u -> u.getDisplayName()).orElse("Unknown");

        return new ClanDTO(clan.getId(), clan.getName(), clan.getTier(),
                clan.getTotalScore().doubleValue(), clan.getLeaderId(), leaderName, memberCount);
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

        int memberCount = clanMemberRepository.findByClanId(clan.getId()).size();
        String leaderName = userRepository.findById(clan.getLeaderId()).map(u -> u.getDisplayName()).orElse("Unknown");

        return new ClanDTO(clan.getId(), clan.getName(), clan.getTier(),
                clan.getTotalScore().doubleValue(), clan.getLeaderId(), leaderName, memberCount);
    }

    @Transactional
    public void leaveClan(UUID userId, UUID clanId) {
        Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new RuntimeException("Clan not found"));

        if (clan.getLeaderId().equals(userId)) {
            throw new RuntimeException("Leader cannot leave the clan. Transfer leadership first or delete the clan.");
        }

        clanMemberRepository.deleteByClanIdAndUserId(clanId, userId);
    }

    @Transactional
    public void deleteClan(UUID clanId, UUID requestingUserId) {
        Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new RuntimeException("Clan not found"));

        if (!clan.getLeaderId().equals(requestingUserId)) {
            throw new RuntimeException("Only the clan leader can delete the clan");
        }

        clanMemberRepository.deleteAll(clanMemberRepository.findByClanId(clanId));
        clanRepository.delete(clan);
    }

    public Optional<ClanDTO> getClanById(UUID clanId) {
        List<Clan> allClans = clanRepository.findAllOrderByTotalScoreDesc();
        return clanRepository.findById(clanId).map(clan -> {
            int memberCount = clanMemberRepository.findByClanId(clan.getId()).size();
            String leaderName = userRepository.findById(clan.getLeaderId()).map(u -> u.getDisplayName()).orElse("Unknown");
            ClanDTO dto = new ClanDTO(clan.getId(), clan.getName(), clan.getTier(),
                    clan.getTotalScore().doubleValue(), clan.getLeaderId(), leaderName, memberCount);
            calculateTierPreview(clan, allClans, dto);
            return dto;
        });
    }

    public List<ClanDTO> getAllClans() {
        List<Clan> allClans = clanRepository.findAllOrderByTotalScoreDesc();
        return allClans.stream().map(clan -> {
            int memberCount = clanMemberRepository.findByClanId(clan.getId()).size();
            String leaderName = userRepository.findById(clan.getLeaderId()).map(u -> u.getDisplayName()).orElse("Unknown");
            ClanDTO dto = new ClanDTO(clan.getId(), clan.getName(), clan.getTier(),
                    clan.getTotalScore().doubleValue(), clan.getLeaderId(), leaderName, memberCount);
            calculateTierPreview(clan, allClans, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ClanLeaderboardDTO> getLeaderboard() {
        List<Clan> clans = clanRepository.findAllOrderByTotalScoreDesc();
        List<ClanLeaderboardDTO> leaderboard = new ArrayList<>();

        for (Clan clan : clans) {
            int memberCount = clanMemberRepository.findByClanId(clan.getId()).size();
            double multiplier = calculateMultiplier(clan, memberCount);
            leaderboard.add(new ClanLeaderboardDTO(
                    clan.getId(), clan.getName(), clan.getTier(),
                    clan.getTotalScore().doubleValue(), memberCount, multiplier
            ));
        }

        leaderboard.sort((a, b) -> Double.compare(b.getEffectiveScore(), a.getEffectiveScore()));
        return leaderboard;
    }

    public List<ClanLeaderboardDTO> getLeaderboardByTier(String tier) {
        List<Clan> clans = clanRepository.findByTierOrderByTotalScoreDesc(tier.toLowerCase());
        List<ClanLeaderboardDTO> leaderboard = new ArrayList<>();

        for (Clan clan : clans) {
            int memberCount = clanMemberRepository.findByClanId(clan.getId()).size();
            double multiplier = calculateMultiplier(clan, memberCount);
            leaderboard.add(new ClanLeaderboardDTO(
                    clan.getId(), clan.getName(), clan.getTier(),
                    clan.getTotalScore().doubleValue(), memberCount, multiplier
            ));
        }

        leaderboard.sort((a, b) -> Double.compare(b.getEffectiveScore(), a.getEffectiveScore()));
        return leaderboard;
    }

    private double calculateMultiplier(Clan clan, int memberCount) {
        switch (clan.getTier().toLowerCase()) {
            case "diamond": return 1.5;
            case "gold": return 1.2;
            case "silver": return 1.1;
            default: return 1.0;
        }
    }

    private int compareTiers(String tier1, String tier2) {
        Map<String, Integer> tierOrder = Map.of(
            "bronze", 1,
            "silver", 2,
            "gold", 3,
            "diamond", 4
        );
        return tierOrder.getOrDefault(tier1.toLowerCase(), 0)
             - tierOrder.getOrDefault(tier2.toLowerCase(), 0);
    }

    private String calculateTierFromPercentile(double percentile) {
        if (percentile <= 0.1) {
            return "diamond";
        } else if (percentile <= 0.3) {
            return "gold";
        } else if (percentile <= 0.6) {
            return "silver";
        } else {
            return "bronze";
        }
    }

    public void calculateTierPreview(Clan clan, List<Clan> allClans, ClanDTO dto) {
        int totalClans = allClans.size();
        if (totalClans == 0) return;

        // Calculate rank (1-based) - count clans with higher score
        int rank = 1;
        for (Clan c : allClans) {
            if (c.getTotalScore().doubleValue() > clan.getTotalScore().doubleValue()) {
                rank++;
            }
        }

        double percentile = (double) rank / totalClans;
        String previewTier = calculateTierFromPercentile(percentile);

        dto.setCurrentTier(clan.getTier());
        dto.setPreviewTier(previewTier);
        dto.setWillPromote(compareTiers(previewTier, clan.getTier()) > 0);
        dto.setWillDemote(compareTiers(previewTier, clan.getTier()) < 0);
    }

    public Optional<ClanDTO> getMyClan(UUID userId) {
        List<ClanMember> memberships = clanMemberRepository.findByUserId(userId);
        if (memberships.isEmpty()) {
            return Optional.empty();
        }
        // Return the first clan (user should only be in one clan)
        ClanMember membership = memberships.get(0);
        return getClanById(membership.getClanId());
    }
}