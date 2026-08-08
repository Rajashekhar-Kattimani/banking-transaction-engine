package com.bank.transaction.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(

        @NotBlank(message = "Source account is required")
        String fromAccount,

        @NotBlank(message = "Destination account is required")
        String toAccount,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Transfer amount must be greater than zero")
        BigDecimal amount
) {
}