package com.yomu.core.controller;

import com.yomu.core.dto.UserMissionDTO;
import com.yomu.core.service.MissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @PostMapping("/{missionId}/claim")
    public ResponseEntity<UserMissionDTO> claimMission(@PathVariable UUID missionId, Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(missionService.claimMission(userId, missionId));
    }
}
