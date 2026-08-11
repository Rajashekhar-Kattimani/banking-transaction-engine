package com.bank.account.dto.request;

import com.bank.account.entity.AccountStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateAccountRequest(

    @NotNull(message = "Account status is required")
    AccountStatus status
) {
}