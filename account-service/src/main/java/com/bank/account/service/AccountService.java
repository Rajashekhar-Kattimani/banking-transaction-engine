package com.bank.account.service;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.request.UpdateAccountRequest;
import com.bank.account.dto.response.AccountResponse;

public interface AccountService {

    AccountResponse create(
            CreateAccountRequest request);

    AccountResponse getById(
            UUID accountId);

    List<AccountResponse> getMyAccounts();

    AccountResponse update(
            UUID accountId,
            UpdateAccountRequest request);

    void close(
            UUID accountId);

    // New operations used by transaction-service
    AccountResponse getByAccountNumber(String accountNumber);

    void debitByAccountNumber(String accountNumber, BigDecimal amount);

    void creditByAccountNumber(String accountNumber, BigDecimal amount);
}