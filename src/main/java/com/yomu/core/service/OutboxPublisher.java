package com.yomu.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yomu.core.entity.OutboxEvent;
import com.yomu.core.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository,
                           RabbitTemplate rabbitTemplate,
                           ObjectMapper objectMapper,
                           @Value("${rabbitmq.exchange}") String exchange) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> unpublished = outboxEventRepository.findUnpublished();
        if (unpublished.isEmpty()) {
            return;
        }

        log.info("Processing {} unpublished outbox events", unpublished.size());

        for (OutboxEvent event : unpublished) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);
                String routingKey = event.getEventType();

                rabbitTemplate.convertAndSend(exchange, routingKey, payload);

                outboxEventRepository.markPublished(event.getId(), OffsetDateTime.now());
                log.info("Published outbox event {} with routing key {}", event.getId(), routingKey);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                outboxEventRepository.incrementAttempts(event.getId(), e.getMessage());
            }
        }
    }
}