package com.yomu.core.controller;

import com.yomu.core.dto.MissionDTO;
import com.yomu.core.service.MissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<MissionDTO>> getUserMissions(@PathVariable UUID userId) {
        return ResponseEntity.ok(missionService.getUserMissions(userId));
    }

    @PostMapping("/{missionId}/claim")
    public ResponseEntity<MissionDTO> claimMission(
            @PathVariable UUID missionId,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(missionService.claimMission(userId, missionId));
    }

    @PostMapping("/initialize")
    public ResponseEntity<Void> initializeMissions(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        missionService.initializeDailyMissions(userId);
        return ResponseEntity.ok().build();
    }
}