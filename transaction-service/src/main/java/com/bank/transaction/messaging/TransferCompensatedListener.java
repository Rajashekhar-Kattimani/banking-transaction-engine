package com.bank.transaction.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bank.messaging.constant.KafkaTopics;
import com.bank.messaging.event.TransferCompensatedEvent;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferCompensatedListener {

    private final TransactionRepository transactionRepository;

    @KafkaListener(
            topics = KafkaTopics.TRANSFER_COMPENSATED,
            groupId = "transaction-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handle(TransferCompensatedEvent event) {

        log.info(
                "Received transfer.compensated: transactionId={}, "
                        + "transactionReference={}, account={}, amount={}",
                event.transactionId(),
                event.transactionReference(),
                event.accountNumber(),
                event.amount()
        );

        Transaction transaction =
                transactionRepository.findById(event.transactionId())
                        .orElse(null);

        if (transaction == null) {

            log.warn(
                    "Transaction not found for transfer.compensated: "
                            + "transactionId={}",
                    event.transactionId()
            );

            return;
        }

        /*
         * Idempotency:
         *
         * Compensation event may be delivered more than once.
         */
        if (transaction.getStatus() == TransactionStatus.COMPENSATED) {

            log.info(
                    "Duplicate transfer.compensated ignored: "
                            + "transactionId={}",
                    event.transactionId()
            );

            return;
        }

        /*
         * Compensation should only happen after the transaction
         * has entered FAILED state.
         */
        if (transaction.getStatus() != TransactionStatus.FAILED) {

            log.warn(
                    "Unexpected transaction status for "
                            + "transfer.compensated: transactionId={}, status={}",
                    event.transactionId(),
                    transaction.getStatus()
            );

            return;
        }

        transaction.setStatus(TransactionStatus.COMPENSATED);

        transactionRepository.save(transaction);

        log.info(
                "Transaction marked as COMPENSATED: "
                        + "transactionId={}, transactionReference={}",
                transaction.getId(),
                transaction.getTransactionReference()
        );
    }
}
