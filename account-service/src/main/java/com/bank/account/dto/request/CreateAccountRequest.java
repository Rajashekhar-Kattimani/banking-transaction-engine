package com.bank.account.dto.request;

import java.math.BigDecimal;

import com.bank.account.entity.AccountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

    @NotNull(message = "Account type is required")
    AccountType accountType,

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must contain 3 characters")
    String currency,

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(
        value = "0.00",
        inclusive = true,
        message = "Initial deposit cannot be negative"
    )
    BigDecimal initialDeposit
) {
}