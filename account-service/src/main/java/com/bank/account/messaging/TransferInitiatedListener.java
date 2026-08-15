package com.bank.account.messaging;

import java.time.Instant;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.bank.account.entity.ProcessedTransfer;
import com.bank.account.repository.ProcessedTransferRepository;
import com.bank.account.service.AccountService;
import com.bank.messaging.constant.KafkaTopics;
import com.bank.messaging.event.TransferDebitedEvent;
import com.bank.messaging.event.TransferFailedEvent;
import com.bank.messaging.event.TransferInitiatedEvent;
import com.bank.messaging.producer.KafkaEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferInitiatedListener {

    private static final String OPERATION_DEBIT = "DEBIT";

    private final AccountService accountService;
    private final KafkaEventPublisher eventPublisher;
    private final ProcessedTransferRepository processedTransferRepository;

    @KafkaListener(
            topics = KafkaTopics.TRANSFER_INITIATED,
            groupId = "account-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(TransferInitiatedEvent event) {

        log.info(
                "Received transfer.initiated: transactionId={}, fromAccount={}, toAccount={}, amount={}",
                event.transactionId(),
                event.fromAccount(),
                event.toAccount(),
                event.amount()
        );

        if (processedTransferRepository.existsByTransactionIdAndOperation(
                event.transactionId(),
                OPERATION_DEBIT)) {

            log.info(
                    "Duplicate debit ignored: transactionId={}",
                    event.transactionId()
            );

            return;
        }

        try {

            log.info(
                    "Debiting account: account={}, amount={}",
                    event.fromAccount(),
                    event.amount()
            );

            accountService.debitForTransfer(
                    event.fromAccount(),
                    event.amount()
            );

            log.info(
                    "Account debited successfully: account={}, transactionId={}",
                    event.fromAccount(),
                    event.transactionId()
            );

            ProcessedTransfer processedTransfer =
                    new ProcessedTransfer(
                            UUID.randomUUID(),
                            event.transactionId(),
                            OPERATION_DEBIT,
                            Instant.now()
                    );

            processedTransferRepository.save(processedTransfer);

            TransferDebitedEvent debitedEvent =
                    new TransferDebitedEvent(
                            UUID.randomUUID(),
                            event.transactionId(),
                            event.transactionReference(),
                            event.fromAccount(),
                            event.toAccount(),
                            event.amount(),
                            Instant.now()
                    );

            log.info(
                    "Publishing transfer.debited: transactionId={}",
                    event.transactionId()
            );

            eventPublisher.publish(
                    KafkaTopics.TRANSFER_DEBITED,
                    event.transactionId().toString(),
                    debitedEvent
            );

            log.info(
                    "transfer.debited published successfully: transactionId={}",
                    event.transactionId()
            );

        } catch (Exception ex) {

            log.error(
                    "Transfer debit failed: transactionId={}, fromAccount={}, reason={}",
                    event.transactionId(),
                    event.fromAccount(),
                    ex.getMessage(),
                    ex
            );

            TransferFailedEvent failedEvent =
                    new TransferFailedEvent(
                            UUID.randomUUID(),
                            event.transactionId(),
                            event.transactionReference(),
                            event.fromAccount(),
                            event.toAccount(),
                            event.amount(),
                            ex.getMessage(),
                            false,
                            Instant.now()
                    );

            eventPublisher.publish(
                    KafkaTopics.TRANSFER_FAILED,
                    event.transactionId().toString(),
                    failedEvent
            );

            log.info(
                    "transfer.failed published: transactionId={}",
                    event.transactionId()
            );
        }
    }
}