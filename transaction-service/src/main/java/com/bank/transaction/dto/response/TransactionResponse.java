package com.bank.transaction.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.entity.TransactionType;

public record TransactionResponse(

        UUID id,

        String transactionReference,

        String fromAccount,

        String toAccount,

        BigDecimal amount,

        TransactionType transactionType,

        TransactionStatus status,

        String failureReason,

        Instant createdAt,

        Instant updatedAt
) {
}