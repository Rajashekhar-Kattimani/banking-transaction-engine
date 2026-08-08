package com.bank.transaction.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.transaction.entity.Transaction;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByTransactionReference(
            String transactionReference);

    boolean existsByTransactionReference(
            String transactionReference);
}