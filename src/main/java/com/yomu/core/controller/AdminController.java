package com.yomu.core.controller;

import com.yomu.core.dto.CreateMissionRequest;
import com.yomu.core.dto.DailyMissionDTO;
import com.yomu.core.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Daily Missions CRUD
    @GetMapping("/missions")
    public ResponseEntity<List<DailyMissionDTO>> getAllMissions() {
        return ResponseEntity.ok(adminService.getAllMissions());
    }

    @GetMapping("/missions/active")
    public ResponseEntity<List<DailyMissionDTO>> getActiveMissions() {
        return ResponseEntity.ok(adminService.getActiveMissions());
    }

    @PostMapping("/missions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DailyMissionDTO> createMission(@Valid @RequestBody CreateMissionRequest request) {
        return ResponseEntity.ok(adminService.createMission(request));
    }

    @PutMapping("/missions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DailyMissionDTO> updateMission(
            @PathVariable UUID id,
            @Valid @RequestBody CreateMissionRequest request) {
        return ResponseEntity.ok(adminService.updateMission(id, request));
    }

    @DeleteMapping("/missions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMission(@PathVariable UUID id) {
        adminService.deleteMission(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/missions/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DailyMissionDTO> toggleMissionActive(
            @PathVariable UUID id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(adminService.toggleMissionActive(id, active));
    }
}