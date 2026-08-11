package com.bank.transaction.service;

import java.util.UUID;

import com.bank.transaction.dto.request.TransferRequest;
import com.bank.transaction.dto.response.TransactionResponse;

public interface TransactionService {

    TransactionResponse transfer(TransferRequest request);

    TransactionResponse getTransaction(UUID transactionId);

    TransactionResponse getTransactionByReference(
            String transactionReference);
}