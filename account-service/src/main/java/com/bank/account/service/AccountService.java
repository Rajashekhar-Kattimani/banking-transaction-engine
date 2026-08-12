package com.bank.account.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.request.UpdateAccountRequest;
import com.bank.account.dto.response.AccountResponse;

public interface AccountService {

	AccountResponse create(CreateAccountRequest request);

	AccountResponse getById(UUID accountId);

	List<AccountResponse> getMyAccounts();

	AccountResponse update(UUID accountId, UpdateAccountRequest request);

	void close(UUID accountId);

	/*
	 * Operations exposed through the account REST API. These operations require the
	 * authenticated user.
	 */
	AccountResponse getByAccountNumber(String accountNumber);

	void debitByAccountNumber(String accountNumber, BigDecimal amount);

	void creditByAccountNumber(String accountNumber, BigDecimal amount);

	void creditByAccountId(UUID accountId, BigDecimal amount);

	/*
	 * Internal transfer operations.
	 *
	 * These are used by Kafka consumers and MUST NOT depend on
	 * SecurityContextHolder because Kafka processing does not have an HTTP
	 * authentication context.
	 */
	void debitForTransfer(String accountNumber, BigDecimal amount);

	void creditForTransfer(String accountNumber, BigDecimal amount);
}