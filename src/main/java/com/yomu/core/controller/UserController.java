package com.yomu.core.controller;

import com.yomu.core.dto.UpdateUserRequest;
import com.yomu.core.dto.UserProfileDTO;
import com.yomu.core.dto.UserDTO;
import com.yomu.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        UUID requestingUserId = UUID.fromString((String) authentication.getPrincipal());

        // Users can only update their own profile (or admins)
        if (!id.equals(requestingUserId) && !"admin".equals(requestingUserId.toString())) {
            return ResponseEntity.status(403).build();
        }

        return userService.updateUser(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}