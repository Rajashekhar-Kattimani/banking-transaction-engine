package com.bank.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferCompletedEvent(
        UUID transactionId,
        String transactionReference,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        Instant timestamp) {
}