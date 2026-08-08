package com.bank.transaction.client.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(

    UUID id,

    String accountNumber,

    UUID customerId,

    String accountType,

    String status,

    String currency,

    BigDecimal balance,

    Instant createdAt,

    Instant updatedAt
) {
}
