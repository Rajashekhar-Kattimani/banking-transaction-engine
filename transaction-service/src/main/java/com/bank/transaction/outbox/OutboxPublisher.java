package com.bank.transaction.outbox;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bank.messaging.producer.KafkaEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaEventPublisher kafkaEventPublisher;

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
                 * For the first implementation we publish the
                 * already serialized JSON payload directly.
                 *
                 * KafkaEventPublisher accepts Object, so the JSON
                 * string is sent as the message value.
                 */
                kafkaEventPublisher.publish(
                        event.getTopic(),
                        event.getEventKey(),
                        event.getPayload()
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
}