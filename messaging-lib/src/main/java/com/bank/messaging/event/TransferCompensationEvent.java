package com.bank.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferCompensationEvent(
        UUID eventId,
        UUID transactionId,
        String transactionReference,
        String accountNumber,
        BigDecimal amount,
        String reason,
        Instant timestamp) {
}