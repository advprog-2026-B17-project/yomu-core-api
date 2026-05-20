package com.yomu.core.controller;

import com.yomu.core.dto.ClanDTO;
import com.yomu.core.service.ClanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clans")
public class ClanController {

    private final ClanService clanService;

    public ClanController(ClanService clanService) {
        this.clanService = clanService;
    }

    @PostMapping
    public ResponseEntity<ClanDTO> createClan(@RequestParam(required = false) String name,
                                              @RequestBody(required = false) Map<String, String> body,
                                              Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String clanName = name != null ? name : body != null ? body.get("name") : null;
        return ResponseEntity.ok(clanService.createClan(userId, clanName));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ClanDTO> joinClan(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(clanService.joinClan(userId, id));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveClan(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        clanService.leaveClan(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClan(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        clanService.deleteClan(userId, id, isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
