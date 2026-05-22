package com.yomu.core.service;

import com.yomu.core.entity.ClanMember;
import com.yomu.core.repository.ClanMemberRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClanNotificationService {

    private final JdbcTemplate jdbcTemplate;
    private final ClanMemberRepository clanMemberRepository;

    public ClanNotificationService(JdbcTemplate jdbcTemplate, ClanMemberRepository clanMemberRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.clanMemberRepository = clanMemberRepository;
    }

    public void notifyUser(UUID userId, String type, String title, String message) {
        jdbcTemplate.update(
            "INSERT INTO gamification.notifications (id, user_id, notification_type, title, message, is_read, created_at) VALUES (?, ?, ?, ?, ?, false, NOW())",
            UUID.randomUUID(), userId, type, title, message
        );
    }

    public void notifyClanMembers(UUID clanId, UUID excludeUserId, String type, String title, String message) {
        List<ClanMember> members = clanMemberRepository.findByClanId(clanId);
        for (ClanMember member : members) {
            if (!member.getUserId().equals(excludeUserId)) {
                notifyUser(member.getUserId(), type, title, message);
            }
        }
    }
}
