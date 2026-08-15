package com.bank.transaction.outbox;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bank.messaging.producer.KafkaEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaEventPublisher kafkaEventPublisher;

    private final ObjectMapper objectMapper;

    /*
     * Poll the outbox every 2 seconds.
     */
    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(
                        OutboxEventStatus.PENDING
                );

        for (OutboxEvent event : events) {

            try {

                log.info(
                        "Publishing outbox event: id={}, eventType={}, topic={}, aggregateId={}",
                        event.getId(),
                        event.getEventType(),
                        event.getTopic(),
                        event.getAggregateId()
                );

                /*
                 * Convert the JSON stored in the outbox
                 * back into the original event object.
                 */
                Object eventObject =
                        deserializeEvent(
                                event.getEventType(),
                                event.getPayload()
                        );

                /*
                 * Publish the actual event object.
                 *
                 * JsonSerializer will now see:
                 *
                 * TransferInitiatedEvent
                 *
                 * instead of:
                 *
                 * String
                 */
                kafkaEventPublisher.publish(
                        event.getTopic(),
                        event.getEventKey(),
                        eventObject
                );

                event.setStatus(OutboxEventStatus.PUBLISHED);

                event.setPublishedAt(Instant.now());

                outboxEventRepository.save(event);

                log.info(
                        "Outbox event published: id={}, topic={}",
                        event.getId(),
                        event.getTopic()
                );

            } catch (Exception ex) {

                event.setRetryCount(
                        event.getRetryCount() + 1
                );

                event.setLastError(
                        ex.getMessage()
                );

                outboxEventRepository.save(event);

                log.error(
                        "Failed to publish outbox event: id={}, retryCount={}, reason={}",
                        event.getId(),
                        event.getRetryCount(),
                        ex.getMessage(),
                        ex
                );
            }
        }
    }

    private Object deserializeEvent(
            String eventType,
            String payload) {

        try {

            return switch (eventType) {

                case "TransferInitiatedEvent" ->
                        objectMapper.readValue(
                                payload,
                                com.bank.messaging.event.TransferInitiatedEvent.class
                        );

                case "TransferDebitedEvent" ->
                        objectMapper.readValue(
                                payload,
                                com.bank.messaging.event.TransferDebitedEvent.class
                        );

                case "TransferCompletedEvent" ->
                        objectMapper.readValue(
                                payload,
                                com.bank.messaging.event.TransferCompletedEvent.class
                        );

                case "TransferFailedEvent" ->
                        objectMapper.readValue(
                                payload,
                                com.bank.messaging.event.TransferFailedEvent.class
                        );

                default ->
                        throw new IllegalArgumentException(
                                "Unknown event type: " + eventType
                        );
            };

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to deserialize outbox event: " + eventType,
                    ex
            );
        }
    }
}