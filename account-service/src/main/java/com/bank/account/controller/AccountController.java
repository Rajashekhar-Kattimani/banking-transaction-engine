package com.bank.account.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.request.UpdateAccountRequest;
import com.bank.account.dto.response.AccountResponse;
import com.bank.account.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody CreateAccountRequest request) {

        AccountResponse response =
                accountService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponse> getById(
            @PathVariable("accountId") UUID accountId) {

        return ResponseEntity.ok(
                accountService.getById(accountId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {

        return ResponseEntity.ok(
                accountService.getMyAccounts());
    }

    @PutMapping("/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponse> update(
            @PathVariable("accountId") UUID accountId,
            @Valid @RequestBody UpdateAccountRequest request) {

        return ResponseEntity.ok(
                accountService.update(
                        accountId,
                        request));
    }

    @DeleteMapping("/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> close(
            @PathVariable("accountId") UUID accountId) {

        accountService.close(accountId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{accountId}/debit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> debitById(
            @PathVariable("accountId") UUID accountId,
            @RequestBody java.math.BigDecimal amount) {

        accountService.debitByAccountId(accountId, amount);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{accountId}/credit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> creditById(
            @PathVariable("accountId") UUID accountId,
            @RequestBody java.math.BigDecimal amount) {

        accountService.creditByAccountId(accountId, amount);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponse> getByAccountNumber(
            @PathVariable("accountNumber") String accountNumber) {

        return ResponseEntity.ok(
                accountService.getByAccountNumber(accountNumber));
    }

    @PostMapping("/number/{accountNumber}/debit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> debitByAccountNumber(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody java.math.BigDecimal amount) {

        accountService.debitByAccountNumber(accountNumber, amount);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/number/{accountNumber}/credit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> creditByAccountNumber(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody java.math.BigDecimal amount) {

        accountService.creditByAccountNumber(accountNumber, amount);

        return ResponseEntity.noContent().build();
    }
}