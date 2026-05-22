package com.yomu.core.service;

import com.yomu.core.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserDeletionService {

    private final EventPublisher eventPublisher;

    public UserDeletionService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void cascadeDelete(UUID userId) {
        eventPublisher.publishUserDeleted(userId);
    }
}