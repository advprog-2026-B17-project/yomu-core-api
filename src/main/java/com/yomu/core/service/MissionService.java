package com.yomu.core.service;

import com.yomu.core.dto.UserMissionDTO;
import com.yomu.core.entity.DailyMission;
import com.yomu.core.entity.UserMission;
import com.yomu.core.repository.DailyMissionRepository;
import com.yomu.core.repository.UserMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class MissionService {

    private final DailyMissionRepository dailyMissionRepository;
    private final UserMissionRepository userMissionRepository;
    private final EventPublisher eventPublisher;

    public MissionService(DailyMissionRepository dailyMissionRepository,
                          UserMissionRepository userMissionRepository,
                          EventPublisher eventPublisher) {
        this.dailyMissionRepository = dailyMissionRepository;
        this.userMissionRepository = userMissionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UserMissionDTO claimMission(UUID userId, UUID missionId) {
        DailyMission mission = dailyMissionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found"));

        UserMission userMission = userMissionRepository
                .findByUserIdAndMissionIdAndDate(userId, missionId, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("Mission has no progress today"));

        if (Boolean.TRUE.equals(userMission.getClaimed())) {
            throw new RuntimeException("Mission reward already claimed");
        }
        if (userMission.getProgress() < mission.getTargetCount()) {
            throw new RuntimeException("Mission is not complete");
        }

        userMission.setClaimed(true);
        userMission = userMissionRepository.save(userMission);
        eventPublisher.publishMissionProgress(
                userId,
                missionId,
                userMission.getProgress(),
                mission.getTargetCount(),
                mission.getXpReward()
        );

        return new UserMissionDTO(
                userMission.getId(),
                userMission.getMissionId(),
                userMission.getProgress(),
                mission.getTargetCount(),
                mission.getXpReward(),
                userMission.getClaimed(),
                userMission.getDate()
        );
    }
}
