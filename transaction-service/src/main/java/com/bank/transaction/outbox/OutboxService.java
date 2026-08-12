package com.bank.transaction.outbox;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    public void save(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String topic,
            String eventKey,
            Object event) {

        try {

            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .id(UUID.randomUUID())
                            .aggregateId(aggregateId)
                            .aggregateType(aggregateType)
                            .eventType(eventType)
                            .topic(topic)
                            .eventKey(eventKey)
                            .payload(payload)
                            .status(OutboxEventStatus.PENDING)
                            .createdAt(Instant.now())
                            .retryCount(0)
                            .build();

            outboxEventRepository.save(outboxEvent);

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(
                    "Failed to serialize outbox event: " + eventType,
                    ex
            );
        }
    }
}