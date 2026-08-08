package com.bank.transaction.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.transaction.dto.request.TransferRequest;
import com.bank.transaction.dto.response.TransactionResponse;
import com.bank.transaction.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request) {

        TransactionResponse response =
                transactionService.transfer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                transactionService.getTransaction(id));
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponse>
            getTransactionByReference(
                    @PathVariable String reference) {

        return ResponseEntity.ok(
                transactionService
                        .getTransactionByReference(reference));
    }
}