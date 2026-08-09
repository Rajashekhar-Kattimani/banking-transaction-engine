package com.bank.account.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.bank.account.entity.AccountStatus;
import com.bank.account.entity.AccountType;

public record AccountResponse(

    UUID id,

    String accountNumber,

    UUID customerId,

    AccountType accountType,

    AccountStatus status,

    String currency,

    BigDecimal balance,

    Instant createdAt,

    Instant updatedAt

) implements Serializable {
}