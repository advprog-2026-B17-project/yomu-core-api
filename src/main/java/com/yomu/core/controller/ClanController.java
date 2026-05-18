package com.yomu.core.controller;

import com.yomu.core.dto.ClanDTO;
import com.yomu.core.dto.ClanLeaderboardDTO;
import com.yomu.core.service.ClanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clans")
public class ClanController {

    private final ClanService clanService;

    public ClanController(ClanService clanService) {
        this.clanService = clanService;
    }

    @PostMapping
    public ResponseEntity<ClanDTO> createClan(
            @RequestParam String name,
            Authentication authentication) {
        UUID leaderId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(clanService.createClan(leaderId, name));
    }

    @PostMapping("/{clanId}/join")
    public ResponseEntity<ClanDTO> joinClan(
            @PathVariable UUID clanId,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(clanService.joinClan(userId, clanId));
    }

    @PostMapping("/{clanId}/leave")
    public ResponseEntity<Void> leaveClan(
            @PathVariable UUID clanId,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        clanService.leaveClan(userId, clanId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{clanId}")
    public ResponseEntity<Void> deleteClan(
            @PathVariable UUID clanId,
            Authentication authentication) {
        UUID requestingUserId = UUID.fromString((String) authentication.getPrincipal());
        clanService.deleteClan(clanId, requestingUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{clanId}")
    public ResponseEntity<ClanDTO> getClan(@PathVariable UUID clanId) {
        return clanService.getClanById(clanId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ClanDTO>> getAllClans() {
        return ResponseEntity.ok(clanService.getAllClans());
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<ClanLeaderboardDTO>> getLeaderboard(
            @RequestParam(required = false) String tier) {
        if (tier != null) {
            return ResponseEntity.ok(clanService.getLeaderboardByTier(tier));
        }
        return ResponseEntity.ok(clanService.getLeaderboard());
    }

    @GetMapping("/me")
    public ResponseEntity<ClanDTO> getMyClan(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return clanService.getMyClan(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}