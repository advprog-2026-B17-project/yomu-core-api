package com.yomu.core.service;

import com.yomu.core.entity.Clan;
import com.yomu.core.entity.ClanMember;
import com.yomu.core.entity.UserAchievement;
import com.yomu.core.repository.AchievementRepository;
import com.yomu.core.repository.ClanMemberRepository;
import com.yomu.core.repository.ClanRepository;
import com.yomu.core.repository.UserAchievementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDeletionServiceTest {

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private ClanMemberRepository clanMemberRepository;

    @Mock
    private ClanRepository clanRepository;

    private UserDeletionService deletionService;

    @BeforeEach
    void setUp() {
        deletionService = new UserDeletionService(
                userAchievementRepository,
                clanMemberRepository,
                clanRepository
        );
    }

    @Nested
    @DisplayName("cascadeDelete()")
    class CascadeDelete {

        @Test
        @DisplayName("deletes all user achievements")
        void deletesAllAchievements() {
            UUID userId = UUID.randomUUID();
            List<UserAchievement> achievements = List.of(
                    createUserAchievement(userId, UUID.randomUUID()),
                    createUserAchievement(userId, UUID.randomUUID())
            );
            when(userAchievementRepository.findByUserId(userId)).thenReturn(achievements);
            when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of());

            deletionService.cascadeDelete(userId);

            verify(userAchievementRepository).deleteAll(achievements);
        }

        @Test
        @DisplayName("deletes clan membership when user has no clan")
        void deletesClanMembershipNoClan() {
            UUID userId = UUID.randomUUID();
            when(userAchievementRepository.findByUserId(userId)).thenReturn(List.of());
            when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of());

            deletionService.cascadeDelete(userId);

            verify(clanMemberRepository).findByUserId(userId);
            verify(clanRepository, never()).findById(any());
            verify(clanRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deletes clan when user is clan leader")
        void deletesClanWhenUserIsLeader() {
            UUID userId = UUID.randomUUID();
            UUID clanId = UUID.randomUUID();

            ClanMember member = new ClanMember();
            member.setUserId(userId);
            member.setClanId(clanId);

            Clan clan = new Clan();
            clan.setId(clanId);
            clan.setLeaderId(userId);

            when(userAchievementRepository.findByUserId(userId)).thenReturn(List.of());
            when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
            when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));
            when(clanMemberRepository.findByClanId(clanId)).thenReturn(List.of(member));

            deletionService.cascadeDelete(userId);

            // member is deleted twice: once from findByUserId, once from findByClanId (cascade)
            verify(clanMemberRepository, times(2)).delete(member);
            verify(clanRepository).delete(clan);
        }

        @Test
        @DisplayName("does not delete clan when user is not leader")
        void doesNotDeleteClanWhenNotLeader() {
            UUID userId = UUID.randomUUID();
            UUID clanId = UUID.randomUUID();
            UUID leaderId = UUID.randomUUID();

            ClanMember member = new ClanMember();
            member.setUserId(userId);
            member.setClanId(clanId);

            Clan clan = new Clan();
            clan.setId(clanId);
            clan.setLeaderId(leaderId); // Different user is leader

            when(userAchievementRepository.findByUserId(userId)).thenReturn(List.of());
            when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of(member));
            when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));

            deletionService.cascadeDelete(userId);

            verify(clanMemberRepository).delete(member);
            verify(clanRepository, never()).delete(any());
        }

        @Test
        @DisplayName("handles empty achievements and memberships list")
        void handlesEmptyLists() {
            UUID userId = UUID.randomUUID();
            when(userAchievementRepository.findByUserId(userId)).thenReturn(List.of());
            when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of());

            deletionService.cascadeDelete(userId);

            verify(userAchievementRepository).deleteAll(List.of());
            verify(clanMemberRepository).findByUserId(userId);
        }
    }

    private UserAchievement createUserAchievement(UUID userId, UUID achievementId) {
        UserAchievement ua = new UserAchievement();
        ua.setUserId(userId);
        ua.setAchievementId(achievementId);
        return ua;
    }
}