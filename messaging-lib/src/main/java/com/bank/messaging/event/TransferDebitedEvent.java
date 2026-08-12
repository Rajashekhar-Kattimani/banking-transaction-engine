package com.bank.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferDebitedEvent(
        UUID eventId,
        UUID transactionId,
        String transactionReference,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        Instant timestamp) {
}