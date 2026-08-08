package com.bank.account.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.bank.persistence.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_account_number",
            columnNames = "account_number"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {

    @Column(
        name = "account_number",
        nullable = false,
        unique = true,
        length = 30
    )
    private String accountNumber;

    @Column(
        name = "customer_id",
        nullable = false
    )
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "account_type",
        nullable = false,
        length = 30
    )
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 30
    )
    private AccountStatus status;

    @Column(
        name = "currency",
        nullable = false,
        length = 3
    )
    private String currency;

    @Column(
        name = "balance",
        nullable = false,
        precision = 19,
        scale = 4
    )
    private BigDecimal balance;
}