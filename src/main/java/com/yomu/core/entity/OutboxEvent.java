package com.yomu.core.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", schema = "outbox")
public class OutboxEvent {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_version")
    private Integer eventVersion = 1;

    @Column(name = "aggregate_type")
    private String aggregateType;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "publish_attempts")
    private Integer publishAttempts = 0;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    public OutboxEvent() {}

    public OutboxEvent(UUID id, String eventType, String payload) {
        this.id = id;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Integer getEventVersion() { return eventVersion; }
    public void setEventVersion(Integer eventVersion) { this.eventVersion = eventVersion; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public void setAggregateId(UUID aggregateId) { this.aggregateId = aggregateId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(OffsetDateTime publishedAt) { this.publishedAt = publishedAt; }
    public Integer getPublishAttempts() { return publishAttempts; }
    public void setPublishAttempts(Integer publishAttempts) { this.publishAttempts = publishAttempts; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}