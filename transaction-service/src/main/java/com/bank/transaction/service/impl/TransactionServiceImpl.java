package com.bank.transaction.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.messaging.constant.KafkaTopics;
import com.bank.messaging.event.TransferInitiatedEvent;
import com.bank.transaction.dto.request.TransferRequest;
import com.bank.transaction.dto.response.TransactionResponse;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.entity.TransactionType;
import com.bank.transaction.exception.InvalidTransferException;
import com.bank.transaction.exception.TransactionNotFoundException;
import com.bank.transaction.outbox.OutboxService;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

//    private final KafkaEventPublisher kafkaEventPublisher;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {

        log.info(
                "Fund transfer requested: fromAccount={}, toAccount={}, amount={}",
                request.fromAccount(),
                request.toAccount(),
                request.amount()
        );

        validateTransfer(request);

        String transactionReference = generateTransactionReference();

        Transaction transaction = Transaction.builder()
                .transactionReference(transactionReference)
                .fromAccount(request.fromAccount())
                .toAccount(request.toAccount())
                .amount(request.amount())
                .transactionType(TransactionType.FUND_TRANSFER)
                .status(TransactionStatus.INITIATED)
                .build();

        transaction = transactionRepository.save(transaction);

        log.info(
                "Transaction created: transactionId={}, transactionReference={}",
                transaction.getId(),
                transactionReference
        );

        /*
         * The transaction-service does NOT debit or credit accounts directly.
         *
         * Account-service owns the account balance operations.
         *
         * The transfer.initiated event starts the Saga.
         */
        TransferInitiatedEvent initiatedEvent =
                new TransferInitiatedEvent(
                        UUID.randomUUID(),                 // eventId
                        transaction.getId(),               // transactionId
                        transaction.getTransactionReference(),
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        Instant.now()
                );

        log.info(
                "Saving transfer.initiated to outbox: transactionId={}, transactionReference={}",
                transaction.getId(),
                transactionReference
        );

        outboxService.save(
                transaction.getId(),
                "TRANSACTION",
                "TransferInitiatedEvent",
                KafkaTopics.TRANSFER_INITIATED,
                transaction.getId().toString(),
                initiatedEvent
        );

        log.info(
                "transfer.initiated saved to outbox: transactionId={}",
                transaction.getId()
        );

        /*
         * The transaction is now waiting for the Saga to complete.
         *
         * Do NOT set SUCCESS here.
         *
         * The final status will be updated when
         * transfer.completed / transfer.failed is received.
         */
        transaction.setStatus(TransactionStatus.PROCESSING);

        transactionRepository.save(transaction);

        return toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID transactionId) {

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new TransactionNotFoundException(
                                        "Transaction not found: " + transactionId));

        return toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(
            String transactionReference) {

        Transaction transaction =
                transactionRepository
                        .findByTransactionReference(transactionReference)
                        .orElseThrow(() ->
                                new TransactionNotFoundException(
                                        "Transaction not found: "
                                                + transactionReference));

        return toResponse(transaction);
    }

    private void validateTransfer(TransferRequest request) {

        if (request.fromAccount() == null
                || request.toAccount() == null) {

            throw new InvalidTransferException(
                    "Source and destination accounts are required");
        }

        if (request.fromAccount()
                .equals(request.toAccount())) {

            throw new InvalidTransferException(
                    "Source and destination accounts cannot be the same");
        }

        if (request.amount() == null
                || request.amount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidTransferException(
                    "Transfer amount must be greater than zero");
        }
    }

    private String generateTransactionReference() {

        return "TXN-"
                + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .toUpperCase();
    }

    private TransactionResponse toResponse(
            Transaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getFromAccount(),
                transaction.getToAccount(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getFailureReason(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}