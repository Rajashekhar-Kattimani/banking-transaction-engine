package com.bank.transaction.messaging;

import java.time.Instant;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bank.messaging.constant.KafkaTopics;
import com.bank.messaging.event.TransferCompensationEvent;
import com.bank.messaging.event.TransferFailedEvent;
import com.bank.messaging.producer.KafkaEventPublisher;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferFailedListener {

    private final TransactionRepository transactionRepository;

    private final KafkaEventPublisher eventPublisher;

    @KafkaListener(
            topics = KafkaTopics.TRANSFER_FAILED,
            groupId = "transaction-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handle(TransferFailedEvent event) {

        log.info(
                "Received transfer.failed: transactionId={}, transactionReference={}, "
                        + "fromAccount={}, toAccount={}, amount={}, reason={}, debitCompleted={}",
                event.transactionId(),
                event.transactionReference(),
                event.fromAccount(),
                event.toAccount(),
                event.amount(),
                event.failureReason(),
                event.debitCompleted()
        );

        Transaction transaction =
                transactionRepository.findById(event.transactionId())
                        .orElse(null);

        if (transaction == null) {

            log.warn(
                    "Transaction not found for transfer.failed: transactionId={}",
                    event.transactionId()
            );

            return;
        }

        /*
         * A successful transaction must never be changed back to FAILED.
         */
        if (transaction.getStatus() == TransactionStatus.SUCCESS
                || transaction.getStatus() == TransactionStatus.COMPLETED) {

            log.warn(
                    "Ignoring transfer.failed for completed transaction: "
                            + "transactionId={}, status={}",
                    event.transactionId(),
                    transaction.getStatus()
            );

            return;
        }

        /*
         * Mark the transaction as FAILED.
         *
         * This is only the transaction status.
         * It is NOT used as the compensation idempotency mechanism.
         */
        if (transaction.getStatus() != TransactionStatus.FAILED) {

            transaction.setStatus(TransactionStatus.FAILED);

            transaction.setFailureReason(
                    event.failureReason()
            );

            transactionRepository.save(transaction);

            log.info(
                    "Transaction marked as FAILED: transactionId={}, reference={}",
                    transaction.getId(),
                    transaction.getTransactionReference()
            );
        }

        /*
         * No debit means there is nothing to compensate.
         */
        if (!event.debitCompleted()) {

            log.info(
                    "No compensation required: debitCompleted=false, "
                            + "transactionId={}",
                    event.transactionId()
            );

            return;
        }

        /*
         * IMPORTANT:
         *
         * Compensation publication must be idempotent.
         *
         * The transaction status FAILED cannot be used for this check,
         * because compensation may not have been published yet.
         *
         * The actual compensation operation is handled by account-service.
         */
        log.info(
                "Publishing transfer.compensate: transactionId={}, "
                        + "account={}, amount={}",
                event.transactionId(),
                event.fromAccount(),
                event.amount()
        );

        TransferCompensationEvent compensationEvent =
                new TransferCompensationEvent(
                        event.eventId(),
                        event.transactionId(),
                        event.transactionReference(),
                        event.fromAccount(),
                        event.amount(),
                        event.failureReason(),
                        Instant.now()
                );

        eventPublisher.publish(
                KafkaTopics.TRANSFER_COMPENSATE,
                event.transactionId().toString(),
                compensationEvent
        );

        log.info(
                "transfer.compensate published successfully: transactionId={}",
                event.transactionId()
        );
    }
}