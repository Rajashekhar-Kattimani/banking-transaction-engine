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
public class AccountServiceImpl
        implements AccountService {

    private final AccountRepository accountRepository;

    private final AccountNumberGenerator
            accountNumberGenerator;

    private final AccountSecurityService
            accountSecurityService;

    @Override
    @Transactional
    public AccountResponse create(
            CreateAccountRequest request) {

        UserPrincipal principal =
                getCurrentPrincipal();

        UUID customerId =
                accountSecurityService
                        .getAuthenticatedUserId(principal);

        Account account = new Account();

        account.setAccountNumber(
                accountNumberGenerator.generate());

        account.setCustomerId(customerId);

        account.setAccountType(
                request.accountType());

        account.setCurrency(
                request.currency().toUpperCase());

        account.setStatus(
                AccountStatus.ACTIVE);

        BigDecimal initialDeposit =
                request.initialDeposit();

        account.setBalance(initialDeposit);

        Account savedAccount =
                accountRepository.save(account);

        return toResponse(savedAccount);
    }

    @Override
    @Cacheable(
        value = "accounts",
        key = "#accountId"
    )
    public AccountResponse getById(
            UUID accountId) {

        UserPrincipal principal =
                getCurrentPrincipal();

        UUID customerId =
                accountSecurityService
                        .getAuthenticatedUserId(principal);

        Account account =
                accountRepository
                        .findByIdAndCustomerId(
                                accountId,
                                customerId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found: "
                                        + accountId));

        return toResponse(account);
    }

    @Override
    public List<AccountResponse> getMyAccounts() {

        UserPrincipal principal =
                getCurrentPrincipal();

        UUID customerId =
                accountSecurityService
                        .getAuthenticatedUserId(principal);

        return accountRepository
                .findAllByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CachePut(
        value = "accounts",
        key = "#accountId"
    )
    public AccountResponse update(
            UUID accountId,
            UpdateAccountRequest request) {

        UserPrincipal principal =
                getCurrentPrincipal();

        UUID customerId =
                accountSecurityService
                        .getAuthenticatedUserId(principal);

        Account account =
                accountRepository
                        .findByIdAndCustomerId(
                                accountId,
                                customerId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found: "
                                        + accountId));

        if (account.getStatus()
                == AccountStatus.CLOSED) {

            throw new AccountOperationException(
                    "Closed account cannot be updated");
        }

        account.setStatus(request.status());

        Account updatedAccount =
                accountRepository.save(account);

        return toResponse(updatedAccount);
    }

    @Override
    @Transactional
    @CacheEvict(
        value = "accounts",
        key = "#accountId"
    )
    public void close(UUID accountId) {

        UserPrincipal principal =
                getCurrentPrincipal();

        UUID customerId =
                accountSecurityService
                        .getAuthenticatedUserId(principal);

        Account account =
                accountRepository
                        .findByIdAndCustomerId(
                                accountId,
                                customerId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found: "
                                        + accountId));

        if (account.getStatus()
                == AccountStatus.CLOSED) {

            throw new AccountOperationException(
                    "Account is already closed");
        }

        if (account.getBalance()
                .compareTo(BigDecimal.ZERO) != 0) {

            throw new AccountOperationException(
                    "Account balance must be zero before "
                    + "closing the account");
        }

        account.setStatus(AccountStatus.CLOSED);

        accountRepository.save(account);
    }

    private UserPrincipal getCurrentPrincipal() {

        if (SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            throw new AccountSecurityException(
                    "Authentication is required");
        }

        Object principal =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        if (!(principal instanceof UserPrincipal userPrincipal)) {

            throw new AccountSecurityException(
                    "Authenticated principal is invalid");
        }

        return userPrincipal;
    }

    private AccountResponse toResponse(
            Account account) {

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getAccountType(),
                account.getStatus(),
                account.getCurrency(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }
}