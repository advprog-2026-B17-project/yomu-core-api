package com.yomu.core.controller;

import com.yomu.core.dto.ClanBuffsResponse;
import com.yomu.core.service.BuffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clans")
public class BuffController {

    private final BuffService buffService;

    public BuffController(BuffService buffService) {
        this.buffService = buffService;
    }

    @GetMapping("/{clanId}/buffs")
    public ResponseEntity<ClanBuffsResponse> getClanBuffs(@PathVariable UUID clanId) {
        return ResponseEntity.ok(buffService.getClanBuffs(clanId));
    }
}