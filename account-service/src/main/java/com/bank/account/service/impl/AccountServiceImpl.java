package com.bank.account.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.request.UpdateAccountRequest;
import com.bank.account.dto.response.AccountResponse;
import com.bank.account.entity.Account;
import com.bank.account.entity.AccountStatus;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.AccountOperationException;
import com.bank.account.exception.AccountSecurityException;
import com.bank.account.repository.AccountRepository;
import com.bank.account.security.AccountSecurityService;
import com.bank.account.service.AccountNumberGenerator;
import com.bank.account.service.AccountService;
import com.bank.security.user.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

	private final AccountRepository accountRepository;

	private final AccountNumberGenerator accountNumberGenerator;

	private final AccountSecurityService accountSecurityService;

	/*
	 * ============================================================ USER ACCOUNT
	 * OPERATIONS ============================================================
	 */

	@Override
	@Transactional
	public AccountResponse create(CreateAccountRequest request) {

		UserPrincipal principal = getCurrentPrincipal();

		UUID customerId = accountSecurityService.getAuthenticatedUserId(principal);

		Account account = new Account();

		account.setAccountNumber(accountNumberGenerator.generate());

		account.setCustomerId(customerId);

		account.setAccountType(request.accountType());

		account.setCurrency(request.currency().toUpperCase());

		account.setStatus(AccountStatus.ACTIVE);

		account.setBalance(request.initialDeposit());

		Account savedAccount = accountRepository.save(account);

		return toResponse(savedAccount);
	}

	@Override
	@Cacheable(value = "accounts", key = "#accountId")
	public AccountResponse getById(UUID accountId) {

		UserPrincipal principal = getCurrentPrincipal();

		UUID customerId = accountSecurityService.getAuthenticatedUserId(principal);

		Account account = accountRepository.findByIdAndCustomerId(accountId, customerId)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

		return toResponse(account);
	}

	@Override
	public List<AccountResponse> getMyAccounts() {

		UserPrincipal principal = getCurrentPrincipal();

		UUID customerId = accountSecurityService.getAuthenticatedUserId(principal);

		return accountRepository.findAllByCustomerId(customerId).stream().map(this::toResponse).toList();
	}

	@Override
	public AccountResponse getByAccountNumber(String accountNumber) {

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

		return toResponse(account);
	}

	@Override
	@Transactional
	@CachePut(value = "accounts", key = "#accountId")
	public AccountResponse update(UUID accountId, UpdateAccountRequest request) {

		UserPrincipal principal = getCurrentPrincipal();

		UUID customerId = accountSecurityService.getAuthenticatedUserId(principal);

		Account account = accountRepository.findByIdAndCustomerId(accountId, customerId)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

		if (account.getStatus() == AccountStatus.CLOSED) {

			throw new AccountOperationException("Closed account cannot be updated");
		}

		if (request.status() == AccountStatus.CLOSED && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {

			throw new AccountOperationException("Account balance must be zero before closing");
		}

		account.setStatus(request.status());

		Account updatedAccount = accountRepository.save(account);

		return toResponse(updatedAccount);
	}

	@Override
	@Transactional
	@CacheEvict(value = "accounts", key = "#accountId")
	public void close(UUID accountId) {

		UserPrincipal principal = getCurrentPrincipal();

		UUID customerId = accountSecurityService.getAuthenticatedUserId(principal);

		Account account = accountRepository.findByIdAndCustomerId(accountId, customerId)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

		if (account.getStatus() == AccountStatus.CLOSED) {

			throw new AccountOperationException("Account is already closed");
		}

		if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {

			throw new AccountOperationException("Account balance must be zero before closing");
		}

		account.setStatus(AccountStatus.CLOSED);

		accountRepository.save(account);
	}

	/*
	 * ============================================================ REST
	 * USER-INITIATED DEBIT
	 * ============================================================
	 */

	@Override
	@Transactional
	@CacheEvict(value = "accounts", allEntries = true)
	public void debitByAccountNumber(String accountNumber, BigDecimal amount) {

		UserPrincipal principal = getCurrentPrincipal();

		UUID customerId = accountSecurityService.getAuthenticatedUserId(principal);

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

		if (!account.getCustomerId().equals(customerId)) {

			throw new AccountSecurityException("You are not authorized to debit this account");
		}

		validateAmount(amount);

		validateActiveAccount(account);

		if (account.getBalance().compareTo(amount) < 0) {

			throw new AccountOperationException("Insufficient balance");
		}

		account.setBalance(account.getBalance().subtract(amount));

		accountRepository.save(account);
	}

	/*
	 * ============================================================ REST
	 * USER-INITIATED CREDIT
	 * ============================================================
	 */

	@Override
	@Transactional
	@CacheEvict(value = "accounts", allEntries = true)
	public void creditByAccountNumber(String accountNumber, BigDecimal amount) {

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

		validateAmount(amount);

		validateActiveAccount(account);

		account.setBalance(account.getBalance().add(amount));

		accountRepository.save(account);
	}

	@Override
	@Transactional
	@CacheEvict(value = "accounts", key = "#accountId")
	public void creditByAccountId(UUID accountId, BigDecimal amount) {

		UserPrincipal principal = getCurrentPrincipal();

		UUID customerId = accountSecurityService.getAuthenticatedUserId(principal);

		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

		if (!account.getCustomerId().equals(customerId)) {

			throw new AccountSecurityException("You are not authorized to credit this account");
		}

		validateAmount(amount);

		validateActiveAccount(account);

		account.setBalance(account.getBalance().add(amount));

		accountRepository.save(account);
	}

	/*
	 * ============================================================ INTERNAL KAFKA
	 * TRANSFER OPERATIONS
	 * ============================================================
	 *
	 * IMPORTANT:
	 *
	 * These methods DO NOT call getCurrentPrincipal().
	 *
	 * Kafka listeners execute outside an HTTP request and therefore there is no
	 * SecurityContext containing the original user.
	 */

	@Override
	@Transactional
	@CacheEvict(value = "accounts", allEntries = true)
	public void debitForTransfer(String accountNumber, BigDecimal amount) {

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

		validateAmount(amount);

		validateActiveAccount(account);

		if (account.getBalance().compareTo(amount) < 0) {

			throw new AccountOperationException("Insufficient balance");
		}

		account.setBalance(account.getBalance().subtract(amount));

		accountRepository.save(account);
	}

	@Override
	@Transactional
	@CacheEvict(value = "accounts", allEntries = true)
	public void creditForTransfer(String accountNumber, BigDecimal amount) {

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

		validateAmount(amount);

		validateActiveAccount(account);

		account.setBalance(account.getBalance().add(amount));

		accountRepository.save(account);
	}

	/*
	 * ============================================================ VALIDATION
	 * ============================================================
	 */

	private void validateAmount(BigDecimal amount) {

		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {

			throw new AccountOperationException("Transfer amount must be greater than zero");
		}
	}

	private void validateActiveAccount(Account account) {

		if (account.getStatus() != AccountStatus.ACTIVE) {

			throw new AccountOperationException("Account is not active: " + account.getAccountNumber());
		}
	}

	/*
	 * ============================================================ SECURITY
	 * ============================================================
	 */

	private UserPrincipal getCurrentPrincipal() {

		if (SecurityContextHolder.getContext().getAuthentication() == null) {

			throw new AccountSecurityException("Authentication is required");
		}

		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (!(principal instanceof UserPrincipal userPrincipal)) {

			throw new AccountSecurityException("Authenticated principal is invalid");
		}

		return userPrincipal;
	}

	/*
	 * ============================================================ RESPONSE MAPPING
	 * ============================================================
	 */

	private AccountResponse toResponse(Account account) {

		return new AccountResponse(account.getId(), account.getAccountNumber(), account.getCustomerId(),
				account.getAccountType(), account.getStatus(), account.getCurrency(), account.getBalance(),
				account.getCreatedAt(), account.getUpdatedAt());
	}
}