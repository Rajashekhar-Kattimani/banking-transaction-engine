package com.bank.account.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.account.entity.ProcessedTransfer;

public interface ProcessedTransferRepository extends JpaRepository<ProcessedTransfer, UUID> {

	boolean existsByTransactionIdAndOperation(UUID transactionId, String operation);
}