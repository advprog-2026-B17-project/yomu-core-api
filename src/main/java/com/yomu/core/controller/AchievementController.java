package com.yomu.core.controller;

import com.yomu.core.dto.AchievementDTO;
import com.yomu.core.service.AchievementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping
    public ResponseEntity<List<AchievementDTO>> getMyAchievements(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(achievementService.getAllAchievementsWithStatus(userId));
    }

    @PatchMapping("/{achievementId}/visibility")
    public ResponseEntity<Void> toggleVisibility(
            @PathVariable UUID achievementId,
            @RequestParam boolean visible,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        achievementService.toggleAchievementVisibility(userId, achievementId, visible);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/visible")
    public ResponseEntity<List<AchievementDTO>> getVisibleAchievements(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(achievementService.getVisibleAchievements(userId));
    }
}