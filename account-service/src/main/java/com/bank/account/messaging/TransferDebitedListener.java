package com.bank.account.messaging;

import java.time.Instant;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.bank.account.entity.ProcessedTransfer;
import com.bank.account.repository.ProcessedTransferRepository;
import com.bank.account.service.AccountService;
import com.bank.messaging.constant.KafkaTopics;
import com.bank.messaging.event.TransferCompletedEvent;
import com.bank.messaging.event.TransferDebitedEvent;
import com.bank.messaging.event.TransferFailedEvent;
import com.bank.messaging.producer.KafkaEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferDebitedListener {

    private static final String OPERATION_CREDIT = "CREDIT";

    private static final String OPERATION_CREDIT_FAILED = "CREDIT_FAILED";

    private final AccountService accountService;

    private final KafkaEventPublisher eventPublisher;

    private final ProcessedTransferRepository processedTransferRepository;

    @KafkaListener(
            topics = KafkaTopics.TRANSFER_DEBITED,
            groupId = "account-service-credit",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(TransferDebitedEvent event) {

        log.info(
                "Received transfer.debited: transactionId={}, "
                        + "fromAccount={}, toAccount={}, amount={}",
                event.transactionId(),
                event.fromAccount(),
                event.toAccount(),
                event.amount()
        );

        /*
         * Idempotency:
         *
         * CREDIT already completed.
         */
        if (processedTransferRepository
                .existsByTransactionIdAndOperation(
                        event.transactionId(),
                        OPERATION_CREDIT)) {

            log.info(
                    "Duplicate transfer.debited ignored because CREDIT "
                            + "already completed: transactionId={}",
                    event.transactionId()
            );

            return;
        }

        /*
         * Idempotency:
         *
         * CREDIT already failed.
         *
         * Do not attempt the destination credit again.
         * Do not generate another transfer.failed event.
         */
        if (processedTransferRepository
                .existsByTransactionIdAndOperation(
                        event.transactionId(),
                        OPERATION_CREDIT_FAILED)) {

            log.info(
                    "Duplicate transfer.debited ignored because CREDIT "
                            + "already failed: transactionId={}",
                    event.transactionId()
            );

            return;
        }

        try {

            log.info(
                    "Crediting destination account: account={}, amount={}",
                    event.toAccount(),
                    event.amount()
            );

            accountService.creditForTransfer(
                    event.toAccount(),
                    event.amount()
            );

            log.info(
                    "Destination account credited successfully: account={}, "
                            + "transactionId={}",
                    event.toAccount(),
                    event.transactionId()
            );

            /*
             * Record successful CREDIT.
             */
            ProcessedTransfer processedTransfer =
                    new ProcessedTransfer(
                            UUID.randomUUID(),
                            event.transactionId(),
                            OPERATION_CREDIT,
                            Instant.now()
                    );

            processedTransferRepository.save(
                    processedTransfer
            );

            /*
             * Publish transfer.completed.
             */
            TransferCompletedEvent completedEvent =
                    new TransferCompletedEvent(
                            event.transactionId(),
                            event.transactionReference(),
                            event.fromAccount(),
                            event.toAccount(),
                            event.amount(),
                            Instant.now()
                    );

            log.info(
                    "Publishing transfer.completed: transactionId={}",
                    event.transactionId()
            );

            eventPublisher.publish(
                    KafkaTopics.TRANSFER_COMPLETED,
                    event.transactionId().toString(),
                    completedEvent
            );

            log.info(
                    "transfer.completed published successfully: "
                            + "transactionId={}",
                    event.transactionId()
            );

        } catch (Exception ex) {

            log.error(
                    "Transfer credit failed: transactionId={}, "
                            + "toAccount={}, reason={}",
                    event.transactionId(),
                    event.toAccount(),
                    ex.getMessage(),
                    ex
            );

            /*
             * Record CREDIT_FAILED.
             *
             * This prevents Kafka redelivery of transfer.debited
             * from repeatedly attempting the same credit operation.
             */
            ProcessedTransfer failedProcessing =
                    new ProcessedTransfer(
                            UUID.randomUUID(),
                            event.transactionId(),
                            OPERATION_CREDIT_FAILED,
                            Instant.now()
                    );

            processedTransferRepository.save(
                    failedProcessing
            );

            /*
             * Source account was already debited.
             *
             * Therefore compensation is required.
             */
            TransferFailedEvent failedEvent =
                    new TransferFailedEvent(
                            UUID.randomUUID(),
                            event.transactionId(),
                            event.transactionReference(),
                            event.fromAccount(),
                            event.toAccount(),
                            event.amount(),
                            ex.getMessage(),
                            true,
                            Instant.now()
                    );

            log.info(
                    "Publishing transfer.failed: transactionId={}, "
                            + "debitCompleted=true",
                    event.transactionId()
            );

            eventPublisher.publish(
                    KafkaTopics.TRANSFER_FAILED,
                    event.transactionId().toString(),
                    failedEvent
            );

            log.info(
                    "transfer.failed published successfully: "
                            + "transactionId={}",
                    event.transactionId()
            );
        }
    }
}