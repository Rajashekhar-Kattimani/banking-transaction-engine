package com.bank.transaction.entity;

import java.math.BigDecimal;

import com.bank.persistence.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Transaction extends BaseEntity {

    @Column(name = "transaction_reference",
            nullable = false,
            unique = true,
            length = 100)
    private String transactionReference;

    @Column(name = "from_account",
            nullable = false,
            length = 50)
    private String fromAccount;

    @Column(name = "to_account",
            nullable = false,
            length = 50)
    private String toAccount;

    @Column(name = "amount",
            nullable = false,
            precision = 19,
            scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type",
            nullable = false,
            length = 30)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",
            nullable = false,
            length = 30)
    private TransactionStatus status;

    @Column(name = "failure_reason",
            length = 500)
    private String failureReason;
}