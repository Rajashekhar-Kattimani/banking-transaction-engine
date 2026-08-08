package com.bank.transaction.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.transaction.client.AccountServiceClient;
import com.bank.transaction.client.dto.AccountResponse;
import com.bank.transaction.dto.request.TransferRequest;
import com.bank.transaction.dto.response.TransactionResponse;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.entity.TransactionType;
import com.bank.transaction.exception.AccountOperationException;
import com.bank.transaction.exception.InvalidTransferException;
import com.bank.transaction.exception.TransactionNotFoundException;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl
        implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final AccountServiceClient accountServiceClient;

    @Override
    @Transactional
    public TransactionResponse transfer(
            TransferRequest request) {

        log.info(
                "Fund transfer requested: fromAccount={}, toAccount={}, amount={}",
                request.fromAccount(),
                request.toAccount(),
                request.amount());

        validateTransfer(request);

        String transactionReference =
                generateTransactionReference();

        Transaction transaction =
                Transaction.builder()
                        .transactionReference(
                                transactionReference)
                        .fromAccount(
                                request.fromAccount())
                        .toAccount(
                                request.toAccount())
                        .amount(request.amount())
                        .transactionType(
                                TransactionType.FUND_TRANSFER)
                        .status(
                                TransactionStatus.INITIATED)
                        .build();

        transaction =
                transactionRepository.save(transaction);

        try {

            AccountResponse sourceAccount =
                    accountServiceClient.getAccount(
                            request.fromAccount());

            AccountResponse destinationAccount =
                    accountServiceClient.getAccount(
                            request.toAccount());

            validateAccounts(
                    sourceAccount,
                    destinationAccount,
                    request);

            transaction.setStatus(
                    TransactionStatus.PROCESSING);

            transactionRepository.save(transaction);

            log.info(
                    "Debiting source account: transactionReference={}, account={}, amount={}",
                    transactionReference,
                    request.fromAccount(),
                    request.amount());

            accountServiceClient.debit(
                    request.fromAccount(),
                    request.amount());

            log.info(
                    "Source account debited: transactionReference={}",
                    transactionReference);

            log.info(
                    "Crediting destination account: transactionReference={}, account={}, amount={}",
                    transactionReference,
                    request.toAccount(),
                    request.amount());

            accountServiceClient.credit(
                    request.toAccount(),
                    request.amount());

            log.info(
                    "Destination account credited: transactionReference={}",
                    transactionReference);

            transaction.setStatus(
                    TransactionStatus.SUCCESS);

            transactionRepository.save(transaction);

            log.info(
                    "Fund transfer successful: transactionReference={}",
                    transactionReference);

            return toResponse(transaction);

        } catch (AccountOperationException exception) {

            log.error(
                    "Fund transfer failed: transactionReference={}",
                    transactionReference,
                    exception);

            transaction.setStatus(
                    TransactionStatus.FAILED);

            transaction.setFailureReason(
                    exception.getMessage());

            transactionRepository.save(transaction);

            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(
            UUID transactionId) {

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new TransactionNotFoundException(
                                        "Transaction not found: "
                                                + transactionId));

        return toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(
            String transactionReference) {

        Transaction transaction =
                transactionRepository
                        .findByTransactionReference(
                                transactionReference)
                        .orElseThrow(() ->
                                new TransactionNotFoundException(
                                        "Transaction not found: "
                                                + transactionReference));

        return toResponse(transaction);
    }

    private void validateTransfer(
            TransferRequest request) {

        if (request.fromAccount()
                .equals(request.toAccount())) {

            throw new InvalidTransferException(
                    "Source and destination accounts "
                            + "cannot be the same");
        }

        if (request.amount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidTransferException(
                    "Transfer amount must be greater than zero");
        }
    }

    private void validateAccounts(
            AccountResponse sourceAccount,
            AccountResponse destinationAccount,
            TransferRequest request) {

        if (sourceAccount == null) {

            throw new InvalidTransferException(
                    "Source account not found: "
                            + request.fromAccount());
        }

        if (destinationAccount == null) {

            throw new InvalidTransferException(
                    "Destination account not found: "
                            + request.toAccount());
        }

        if (sourceAccount.balance()
                .compareTo(request.amount()) < 0) {

            throw new InvalidTransferException(
                    "Insufficient balance in source account");
        }

        if (sourceAccount.status() != null
                && !sourceAccount.status()
                        .equalsIgnoreCase("ACTIVE")) {

            throw new InvalidTransferException(
                    "Source account is not active");
        }

        if (destinationAccount.status() != null
                && !destinationAccount.status()
                        .equalsIgnoreCase("ACTIVE")) {

            throw new InvalidTransferException(
                    "Destination account is not active");
        }
    }

    private String generateTransactionReference() {

        return "TXN-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .toUpperCase();
    }

    private TransactionResponse toResponse(
            Transaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getFromAccount(),
                transaction.getToAccount(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getFailureReason(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt());
    }
}