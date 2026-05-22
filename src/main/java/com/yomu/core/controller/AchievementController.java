package com.yomu.core.controller;

import com.yomu.core.dto.AchievementDTO;
import com.yomu.core.dto.AchievementVisibilityDTO;
import com.yomu.core.dto.CreateAchievementRequest;
import com.yomu.core.service.AchievementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping("/api/admin/achievements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AchievementDTO>> getAllAchievements() {
        return ResponseEntity.ok(achievementService.getAllAchievements());
    }

    @PostMapping("/api/admin/achievements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementDTO> createAchievement(@Valid @RequestBody CreateAchievementRequest request) {
        return ResponseEntity.ok(achievementService.createAchievement(request));
    }

    @PutMapping("/api/admin/achievements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementDTO> updateAchievement(@PathVariable UUID id,
            @Valid @RequestBody CreateAchievementRequest request) {
        return ResponseEntity.ok(achievementService.updateAchievement(id, request));
    }

    @DeleteMapping("/api/admin/achievements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAchievement(@PathVariable UUID id) {
        achievementService.deleteAchievement(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/achievements/{achievementId}/visibility")
    public ResponseEntity<AchievementVisibilityDTO> setAchievementVisibility(
            @PathVariable UUID achievementId,
            @RequestParam boolean visible,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());

        return achievementService.setVisibility(userId, achievementId, visible)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
