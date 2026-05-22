package com.yomu.core.service;

import com.yomu.core.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserDeletionService {

    public UserDeletionService() {
        // Default constructor for service instantiation
    }

    @Transactional
    public void cascadeDelete(UUID userId) {
        // Gamification cleanup is handled by yomu-gamification-api via events
        // Core only needs to handle user record deletion
    }
}