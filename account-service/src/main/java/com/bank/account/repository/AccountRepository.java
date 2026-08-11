package com.bank.account.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.account.entity.Account;

public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findAllByCustomerId(UUID customerId);

    Optional<Account> findByIdAndCustomerId(
            UUID id,
            UUID customerId);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByIdAndCustomerId(
            UUID id,
            UUID customerId);
}