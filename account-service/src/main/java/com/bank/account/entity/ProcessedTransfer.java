package com.bank.account.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_transfers", uniqueConstraints = {
		@UniqueConstraint(name = "uk_processed_transfer_operation", columnNames = { "transaction_id", "operation" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedTransfer {

	@Id
	private UUID id;

	@Column(name = "transaction_id", nullable = false)
	private UUID transactionId;

	@Column(name = "operation", nullable = false, length = 30)
	private String operation;

	@Column(name = "processed_at", nullable = false)
	private Instant processedAt;
}