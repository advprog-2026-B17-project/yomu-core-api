package com.yomu.core.service;

import com.yomu.core.dto.BuffDTO;
import com.yomu.core.dto.ClanBuffsResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class BuffService {

    private final JdbcTemplate jdbcTemplate;

    public BuffService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ClanBuffsResponse getClanBuffs(UUID clanId) {
        String sql = """
            SELECT buff_type, multiplier, activated_at
            FROM gamification.buffs
            WHERE clan_id = ? AND expires_at IS NULL
            ORDER BY activated_at DESC
            """;

        List<BuffDTO> buffs = jdbcTemplate.query(sql, (rs, rowNum) -> {
            BuffDTO dto = new BuffDTO();
            dto.setBuffType(rs.getString("buff_type"));
            dto.setMultiplier(rs.getDouble("multiplier"));
            dto.setActivatedAt(rs.getObject("activated_at", OffsetDateTime.class));
            dto.setDescription(getBuffDescription(rs.getString("buff_type"), rs.getDouble("multiplier")));
            return dto;
        }, clanId);

        return new ClanBuffsResponse(clanId, buffs);
    }

    private String getBuffDescription(String buffType, double multiplier) {
        return switch (buffType) {
            case "productivity_buff" -> "50%+ anggota selesaikan misi harian";
            case "low_accuracy_penalty" -> "Rata-rata akurasi <50%";
            case "consistent_reader_buff" -> "Rata-rata akurasi >=80%";
            case "inactive_penalty" -> "<30% anggota aktif dalam 3 hari";
            default -> "Aktif";
        };
    }
}