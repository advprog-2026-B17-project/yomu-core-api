package com.yomu.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Centralized event publisher for RabbitMQ events.
 * Publishes to the yomu.events topic exchange.
 */
@Service
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public EventPublisher(RabbitTemplate rabbitTemplate,
                         @Value("${rabbitmq.exchange}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    /**
     * Publish reading.completed event after a user marks a reading as complete.
     */
    public void publishReadingCompleted(UUID userId, UUID readingId) {
        Map<String, Object> event = buildEvent("reading.completed", Map.of(
            "userId", userId.toString(),
            "readingId", readingId.toString(),
            "completedAt", OffsetDateTime.now().toString()
        ));
        publish("reading.completed", event);
    }

    /**
     * Publish quiz.completed event after a user completes a quiz.
     */
    public void publishQuizCompleted(UUID userId, UUID readingId, int score, double accuracy) {
        Map<String, Object> event = buildEvent("quiz.completed", Map.of(
            "userId", userId.toString(),
            "readingId", readingId.toString(),
            "score", score,
            "accuracy", accuracy,
            "completedAt", OffsetDateTime.now().toString()
        ));
        publish("quiz.completed", event);
    }

    /**
     * Publish mission.progress event after mission progress is updated.
     */
    public void publishMissionProgress(UUID userId, UUID missionId, int progress, int target, int xpReward) {
        Map<String, Object> event = buildEvent("mission.progress", Map.of(
            "userId", userId.toString(),
            "missionId", missionId.toString(),
            "progress", progress,
            "target", target,
            "xpReward", xpReward
        ));
        publish("mission.progress", event);
    }

    /**
     * Publish achievement.unlocked event after an achievement is unlocked.
     * Note: According to EVENTS_CONTRACT.md, this is typically published by yomu-gamification-api,
     * but can be used for cross-service notifications.
     */
    public void publishAchievementUnlocked(UUID userId, UUID achievementId, String achievementName) {
        Map<String, Object> event = buildEvent("achievement.unlocked", Map.of(
            "userId", userId.toString(),
            "achievementId", achievementId.toString(),
            "achievementName", achievementName
        ));
        publish("achievement.unlocked", event);
    }

    /**
     * Publish season.ended event after a season concludes.
     */
    public void publishSeasonEnded(UUID seasonId, List<Map<String, Object>> rankings) {
        Map<String, Object> event = buildEvent("season.ended", Map.of(
            "seasonId", seasonId.toString(),
            "rankings", rankings
        ));
        publish("season.ended", event);
    }

    /**
     * Publish user.created event after a new user registers.
     */
    public void publishUserCreated(UUID userId, String username, String displayName, String role) {
        Map<String, Object> event = buildEvent("user.created", Map.of(
            "userId", userId.toString(),
            "username", username,
            "displayName", displayName,
            "role", role
        ));
        publish("user.created", event);
    }

    /**
     * Publish user.updated event after user profile is updated.
     */
    public void publishUserUpdated(UUID userId, String username, String displayName, String role) {
        Map<String, Object> event = buildEvent("user.updated", Map.of(
            "userId", userId.toString(),
            "username", username,
            "displayName", displayName,
            "role", role
        ));
        publish("user.updated", event);
    }

    /**
     * Publish user.deleted event before user account is deleted.
     */
    public void publishUserDeleted(UUID userId) {
        Map<String, Object> event = buildEvent("user.deleted", Map.of(
            "userId", userId.toString()
        ));
        publish("user.deleted", event);
    }

    private Map<String, Object> buildEvent(String eventType, Map<String, Object> payload) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", eventType);
        event.put("timestamp", OffsetDateTime.now().toString());
        event.put("payload", payload);
        return event;
    }

    private void publish(String routingKey, Map<String, Object> event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published event {} with routing key {}", event.get("eventType"), routingKey);
        } catch (Exception e) {
            log.error("Failed to publish event {}: {}", event.get("eventType"), e.getMessage());
            // Don't fail the main operation - event publishing is async and non-critical
        }
    }
}