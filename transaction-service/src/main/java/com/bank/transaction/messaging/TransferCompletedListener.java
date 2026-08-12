package com.bank.transaction.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bank.messaging.constant.KafkaTopics;
import com.bank.messaging.event.TransferCompletedEvent;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferCompletedListener {

    private final TransactionRepository transactionRepository;

    @KafkaListener(
            topics = KafkaTopics.TRANSFER_COMPLETED,
            groupId = "transaction-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handle(TransferCompletedEvent event) {

        log.info(
                "Received transfer.completed: transactionId={}, "
                        + "transactionReference={}, fromAccount={}, "
                        + "toAccount={}, amount={}",
                event.transactionId(),
                event.transactionReference(),
                event.fromAccount(),
                event.toAccount(),
                event.amount()
        );

        Transaction transaction =
                transactionRepository.findById(event.transactionId())
                        .orElse(null);

        if (transaction == null) {

            log.warn(
                    "Transaction not found for transfer.completed: "
                            + "transactionId={}",
                    event.transactionId()
            );

            return;
        }

        /*
         * Idempotency:
         *
         * If transfer.completed is delivered more than once,
         * do not perform the status update again.
         */
        if (transaction.getStatus() == TransactionStatus.COMPLETED
                || transaction.getStatus() == TransactionStatus.SUCCESS) {

            log.info(
                    "Duplicate transfer.completed ignored: "
                            + "transactionId={}, status={}",
                    event.transactionId(),
                    transaction.getStatus()
            );

            return;
        }

        /*
         * A FAILED transaction must not be changed to COMPLETED.
         *
         * This protects the transaction state machine from
         * out-of-order or unexpected Kafka messages.
         */
        if (transaction.getStatus() == TransactionStatus.FAILED
                || transaction.getStatus() == TransactionStatus.COMPENSATED) {

            log.warn(
                    "Ignoring transfer.completed for transaction in "
                            + "terminal failure state: transactionId={}, status={}",
                    event.transactionId(),
                    transaction.getStatus()
            );

            return;
        }

        /*
         * Expected state:
         *
         * INITIATED -> PROCESSING -> COMPLETED
         */
        if (transaction.getStatus() != TransactionStatus.PROCESSING
                && transaction.getStatus() != TransactionStatus.INITIATED) {

            log.warn(
                    "Unexpected transaction status for transfer.completed: "
                            + "transactionId={}, status={}",
                    event.transactionId(),
                    transaction.getStatus()
            );

            return;
        }

        transaction.setStatus(TransactionStatus.COMPLETED);

        transactionRepository.save(transaction);

        log.info(
                "Transaction marked as COMPLETED: "
                        + "transactionId={}, transactionReference={}",
                transaction.getId(),
                transaction.getTransactionReference()
        );
    }
}