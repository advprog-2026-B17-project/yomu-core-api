package com.yomu.core.service;

import com.yomu.core.dto.CreateMissionRequest;
import com.yomu.core.dto.DailyMissionDTO;
import com.yomu.core.entity.DailyMission;
import com.yomu.core.repository.DailyMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final DailyMissionRepository dailyMissionRepository;

    public AdminService(DailyMissionRepository dailyMissionRepository) {
        this.dailyMissionRepository = dailyMissionRepository;
    }

    public List<DailyMissionDTO> getAllMissions() {
        return dailyMissionRepository.findAll().stream()
                .map(m -> new DailyMissionDTO(m.getId(), m.getTitle(), m.getDescription(),
                        m.getTargetType(), m.getTargetCount(), m.getXpReward(), m.getIsActive()))
                .collect(Collectors.toList());
    }

    public List<DailyMissionDTO> getActiveMissions() {
        return dailyMissionRepository.findByIsActiveTrue().stream()
                .map(m -> new DailyMissionDTO(m.getId(), m.getTitle(), m.getDescription(),
                        m.getTargetType(), m.getTargetCount(), m.getXpReward(), m.getIsActive()))
                .collect(Collectors.toList());
    }

    @Transactional
    public DailyMissionDTO createMission(CreateMissionRequest request) {
        DailyMission mission = new DailyMission();
        mission.setTitle(request.getTitle());
        mission.setDescription(request.getDescription());
        mission.setTargetType(request.getTargetType());
        mission.setTargetCount(request.getTargetCount());
        mission.setXpReward(request.getXpReward() != null ? request.getXpReward() : 10);
        mission.setIsActive(true);
        mission = dailyMissionRepository.save(mission);

        return new DailyMissionDTO(mission.getId(), mission.getTitle(), mission.getDescription(),
                mission.getTargetType(), mission.getTargetCount(), mission.getXpReward(), mission.getIsActive());
    }

    @Transactional
    public DailyMissionDTO updateMission(UUID id, CreateMissionRequest request) {
        DailyMission mission = dailyMissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission not found"));

        mission.setTitle(request.getTitle());
        mission.setDescription(request.getDescription());
        mission.setTargetType(request.getTargetType());
        mission.setTargetCount(request.getTargetCount());
        if (request.getXpReward() != null) {
            mission.setXpReward(request.getXpReward());
        }
        mission = dailyMissionRepository.save(mission);

        return new DailyMissionDTO(mission.getId(), mission.getTitle(), mission.getDescription(),
                mission.getTargetType(), mission.getTargetCount(), mission.getXpReward(), mission.getIsActive());
    }

    @Transactional
    public void deleteMission(UUID id) {
        dailyMissionRepository.deleteById(id);
    }

    @Transactional
    public DailyMissionDTO toggleMissionActive(UUID id, boolean active) {
        DailyMission mission = dailyMissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission not found"));
        mission.setIsActive(active);
        mission = dailyMissionRepository.save(mission);

        return new DailyMissionDTO(mission.getId(), mission.getTitle(), mission.getDescription(),
                mission.getTargetType(), mission.getTargetCount(), mission.getXpReward(), mission.getIsActive());
    }
}