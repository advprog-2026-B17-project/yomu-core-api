package com.yomu.core.service;

import com.yomu.core.dto.ClanDTO;
import com.yomu.core.entity.Clan;
import com.yomu.core.entity.ClanMember;
import com.yomu.core.repository.ClanMemberRepository;
import com.yomu.core.repository.ClanRepository;
import com.yomu.core.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClanService {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository clanMemberRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public ClanService(
            ClanRepository clanRepository,
            ClanMemberRepository clanMemberRepository,
            UserRepository userRepository,
            JdbcTemplate jdbcTemplate) {
        this.clanRepository = clanRepository;
        this.clanMemberRepository = clanMemberRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    private void notifyUser(UUID userId, String type, String title, String message) {
        jdbcTemplate.update(
                "INSERT INTO gamification.notifications (id, user_id, notification_type, title, message, is_read, created_at) VALUES (?, ?, ?, ?, ?, false, NOW())",
                UUID.randomUUID(), userId, type, title, message
        );
    }

    private void notifyClanMembers(UUID clanId, UUID excludeUserId, String type, String title, String message) {
        List<ClanMember> members = clanMemberRepository.findByClanId(clanId);
        for (ClanMember member : members) {
            if (!member.getUserId().equals(excludeUserId)) {
                notifyUser(member.getUserId(), type, title, message);
            }
        }
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

        String joinerName = userRepository.findById(userId).map(u -> u.getDisplayName()).orElse("Seseorang");
        notifyClanMembers(clanId, userId, "clan_event",
                "Anggota Baru di " + clan.getName(),
                joinerName + " bergabung dengan clan kamu!");

        return toDTO(clan, "member");
    }

    @Transactional
    public void leaveClan(UUID userId, UUID clanId) {
        ClanMember member = clanMemberRepository.findByClanIdAndUserId(clanId, userId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this clan"));
        if ("leader".equals(member.getRole())) {
            throw new RuntimeException("Clan leaders must delete the clan instead of leaving it");
        }

        Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new RuntimeException("Clan not found"));

        String leaverName = userRepository.findById(userId).map(u -> u.getDisplayName()).orElse("Seseorang");
        clanMemberRepository.deleteByClanIdAndUserId(clanId, userId);

        notifyClanMembers(clanId, userId, "clan_event",
                "Anggota Keluar dari " + clan.getName(),
                leaverName + " telah keluar dari clan.");
    }

    @Transactional
    public void deleteClan(UUID requesterId, UUID clanId, boolean admin) {
        Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new RuntimeException("Clan not found"));
        if (!admin && !requesterId.equals(clan.getLeaderId())) {
            throw new RuntimeException("Only the clan leader can delete this clan");
        }

        notifyClanMembers(clanId, requesterId, "clan_event",
                "Clan " + clan.getName() + " Dibubarkan",
                "Clan kamu telah dibubarkan oleh pemimpin.");

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

    public Optional<ClanDTO> getMyClan(UUID userId) {
        List<ClanMember> memberships = clanMemberRepository.findByUserId(userId);
        if (memberships.isEmpty()) return Optional.empty();
        return getClanById(memberships.get(0).getClanId());
    }
}
