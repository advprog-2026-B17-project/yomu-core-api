package com.yomu.core.service;

import com.yomu.core.dto.ClanDTO;
import com.yomu.core.entity.Clan;
import com.yomu.core.entity.ClanMember;
import com.yomu.core.entity.User;
import com.yomu.core.repository.ClanMemberRepository;
import com.yomu.core.repository.ClanRepository;
import com.yomu.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClanServiceTest {

    @Mock private ClanRepository clanRepository;
    @Mock private ClanMemberRepository clanMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClanNotificationService clanNotificationService;

    @InjectMocks private ClanService clanService;

    private UUID userId;
    private UUID clanId;
    private Clan clan;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        clanId = UUID.randomUUID();

        clan = new Clan();
        clan.setId(clanId);
        clan.setName("TestClan");
        clan.setTier("bronze");
        clan.setTotalScore(BigDecimal.ZERO);
        clan.setLeaderId(userId);

        user = new User();
        user.setId(userId);
        user.setDisplayName("TestUser");
    }

    @Test
    void createClan_success() {
        when(clanRepository.existsByName("TestClan")).thenReturn(false);
        when(clanRepository.save(any())).thenReturn(clan);
        when(clanMemberRepository.findByClanId(clanId)).thenReturn(List.of());

        ClanDTO result = clanService.createClan(userId, "TestClan");

        assertNotNull(result);
        assertEquals("TestClan", result.getName());
        verify(clanRepository).save(any());
        verify(clanMemberRepository).save(any());
    }

    @Test
    void createClan_duplicateName_throwsException() {
        when(clanRepository.existsByName("TestClan")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                clanService.createClan(userId, "TestClan")
        );
        verify(clanRepository, never()).save(any());
    }

    @Test
    void joinClan_success() {
        UUID newUserId = UUID.randomUUID();
        User newUser = new User();
        newUser.setId(newUserId);
        newUser.setDisplayName("NewUser");

        ClanMember existingMember = new ClanMember();
        existingMember.setUserId(userId);

        when(clanMemberRepository.existsByClanIdAndUserId(clanId, newUserId)).thenReturn(false);
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));
        when(clanMemberRepository.findByClanId(clanId)).thenReturn(List.of(existingMember));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newUser));

        ClanDTO result = clanService.joinClan(newUserId, clanId);

        assertNotNull(result);
        verify(clanMemberRepository).save(any());
        verify(clanNotificationService).notifyClanMembers(eq(clanId), eq(newUserId), any(), any(), any());
    }

    @Test
    void joinClan_alreadyMember_throwsException() {
        when(clanMemberRepository.existsByClanIdAndUserId(clanId, userId)).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                clanService.joinClan(userId, clanId)
        );
    }

    @Test
    void leaveClan_success() {
        UUID memberId = UUID.randomUUID();
        User member = new User();
        member.setId(memberId);
        member.setDisplayName("Member");

        when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertDoesNotThrow(() -> clanService.leaveClan(memberId, clanId));
        verify(clanMemberRepository).deleteByClanIdAndUserId(clanId, memberId);
        verify(clanNotificationService).notifyClanMembers(eq(clanId), eq(memberId), any(), any(), any());
    }

    @Test
    void leaveClan_leaderCannotLeave_throwsException() {
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));

        assertThrows(RuntimeException.class, () ->
                clanService.leaveClan(userId, clanId)
        );
        verify(clanMemberRepository, never()).deleteByClanIdAndUserId(any(), any());
    }

    @Test
    void deleteClan_success() {
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));
        assertDoesNotThrow(() -> clanService.deleteClan(userId, clanId, false));
        verify(clanRepository).delete(clan);
        verify(clanNotificationService).notifyClanMembers(eq(clanId), eq(userId), any(), any(), any());
    }

    @Test
    void deleteClan_notLeader_throwsException() {
        UUID otherId = UUID.randomUUID();
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));

        assertThrows(RuntimeException.class, () ->
                clanService.deleteClan(otherId, clanId, false)
        );
        verify(clanRepository, never()).delete(any());
    }

    @Test
    void getMyClan_noMembership_returnsEmpty() {
        when(clanMemberRepository.findByUserId(userId)).thenReturn(List.of());

        Optional<ClanDTO> result = clanService.getMyClan(userId);

        assertTrue(result.isEmpty());
    }
}