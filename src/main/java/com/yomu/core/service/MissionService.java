package com.yomu.core.service;

import com.yomu.core.dto.MissionDTO;
import com.yomu.core.entity.DailyMission;
import com.yomu.core.entity.UserMission;
import com.yomu.core.repository.DailyMissionRepository;
import com.yomu.core.repository.UserMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MissionService {

    private final UserMissionRepository userMissionRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final EventPublisher eventPublisher;

    public MissionService(UserMissionRepository userMissionRepository,
                         DailyMissionRepository dailyMissionRepository,
                         EventPublisher eventPublisher) {
        this.userMissionRepository = userMissionRepository;
        this.dailyMissionRepository = dailyMissionRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<MissionDTO> getUserMissions(UUID userId) {
        LocalDate today = LocalDate.now();

        // Get all active daily missions
        List<DailyMission> activeMissions = dailyMissionRepository.findByIsActiveTrue();

        // Get user's mission progress for today
        List<UserMission> userMissions = userMissionRepository.findByUserIdAndDate(userId, today);

        // Create a map of mission progress
        return activeMissions.stream()
            .map(dm -> {
                MissionDTO dto = new MissionDTO();
                dto.setId(dm.getId());
                dto.setTitle(dm.getTitle());
                dto.setDescription(dm.getDescription());
                dto.setTargetType(dm.getTargetType());
                dto.setTargetCount(dm.getTargetCount());
                dto.setXpReward(dm.getXpReward());
                dto.setDate(today);

                // Find user's progress for this mission
                UserMission um = userMissions.stream()
                    .filter(u -> u.getMissionId().equals(dm.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        // Auto-create user mission if not exists
                        UserMission newUm = new UserMission();
                        newUm.setUserId(userId);
                        newUm.setMissionId(dm.getId());
                        newUm.setProgress(0);
                        newUm.setClaimed(false);
                        newUm.setDate(today);
                        return userMissionRepository.save(newUm);
                    });

                dto.setProgress(um.getProgress());
                dto.setClaimed(um.getClaimed());

                return dto;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public MissionDTO claimMission(UUID userId, UUID missionId) {
        LocalDate today = LocalDate.now();

        UserMission userMission = userMissionRepository
            .findByUserIdAndMissionIdAndDate(userId, missionId, today)
            .orElseThrow(() -> new RuntimeException("Mission not found for today"));

        if (userMission.getClaimed()) {
            throw new RuntimeException("Mission already claimed");
        }

        DailyMission dm = dailyMissionRepository.findById(missionId)
            .orElseThrow(() -> new RuntimeException("Daily mission not found"));

        if (userMission.getProgress() < dm.getTargetCount()) {
            throw new RuntimeException("Mission not completed yet");
        }

        userMission.setClaimed(true);
        userMissionRepository.save(userMission);

        // Publish mission progress event
        eventPublisher.publishMissionProgress(userId, missionId, 1, 1, dm.getXpReward());

        MissionDTO dto = new MissionDTO();
        dto.setId(missionId);
        dto.setClaimed(true);
        return dto;
    }

    public void initializeDailyMissions(UUID userId) {
        LocalDate today = LocalDate.now();
        List<DailyMission> activeMissions = dailyMissionRepository.findByIsActiveTrue();

        for (DailyMission dm : activeMissions) {
            // Only create if not exists
            if (userMissionRepository.findByUserIdAndMissionIdAndDate(userId, dm.getId(), today).isEmpty()) {
                UserMission um = new UserMission();
                um.setUserId(userId);
                um.setMissionId(dm.getId());
                um.setProgress(0);
                um.setClaimed(false);
                um.setDate(today);
                userMissionRepository.save(um);
            }
        }
    }
}