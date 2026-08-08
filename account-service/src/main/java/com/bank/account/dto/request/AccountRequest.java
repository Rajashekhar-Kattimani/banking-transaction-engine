package com.bank.account.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.bank.account.entity.AccountType;

public record AccountRequest(
        UUID userId,
        AccountType type,
        BigDecimal initialDeposit,
        String currency
) {
}