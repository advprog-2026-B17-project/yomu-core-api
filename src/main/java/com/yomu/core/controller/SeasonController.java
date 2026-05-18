package com.yomu.core.controller;

import com.yomu.core.dto.CreateSeasonRequest;
import com.yomu.core.dto.SeasonDTO;
import com.yomu.core.service.SeasonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/seasons")
public class SeasonController {

    private final SeasonService seasonService;

    public SeasonController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @GetMapping
    public ResponseEntity<List<SeasonDTO>> getAllSeasons() {
        return ResponseEntity.ok(seasonService.getAllSeasons());
    }

    @GetMapping("/active")
    public ResponseEntity<SeasonDTO> getActiveSeason() {
        return seasonService.getActiveSeason()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeasonDTO> createSeason(@Valid @RequestBody CreateSeasonRequest request) {
        return ResponseEntity.ok(seasonService.createSeason(request));
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeasonDTO> endSeason(@PathVariable UUID id) {
        return ResponseEntity.ok(seasonService.endSeason(id).orElseThrow());
    }
}