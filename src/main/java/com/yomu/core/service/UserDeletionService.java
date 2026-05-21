package com.yomu.core.service;

import com.yomu.core.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserDeletionService {

    private final UserAchievementRepository userAchievementRepository;
    private final ClanMemberRepository clanMemberRepository;
    private final ClanRepository clanRepository;

    public UserDeletionService(UserAchievementRepository userAchievementRepository,
                               ClanMemberRepository clanMemberRepository,
                               ClanRepository clanRepository) {
        this.userAchievementRepository = userAchievementRepository;
        this.clanMemberRepository = clanMemberRepository;
        this.clanRepository = clanRepository;
    }

    @Transactional
    public void cascadeDelete(UUID userId) {
        deleteAchievements(userId);
        deleteClanMembership(userId);
    }

    private void deleteAchievements(UUID userId) {
        userAchievementRepository.deleteAll(userAchievementRepository.findByUserId(userId));
    }

    private void deleteClanMembership(UUID userId) {
        clanMemberRepository.findByUserId(userId).forEach(member -> {
            clanMemberRepository.delete(member);
            clanRepository.findById(member.getClanId()).ifPresent(clan -> {
                if (userId.equals(clan.getLeaderId())) {
                    clanMemberRepository.findByClanId(clan.getId()).forEach(clanMemberRepository::delete);
                    clanRepository.delete(clan);
                }
            });
        });
    }
}