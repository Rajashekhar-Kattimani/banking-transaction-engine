package com.bank.account.service.impl;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountNumberGenerator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountNumberGeneratorImpl
        implements AccountNumberGenerator {

    private static final int ACCOUNT_NUMBER_LENGTH = 12;

    private final AccountRepository accountRepository;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {

        String accountNumber;

        do {
            accountNumber = generateNumber();
        } while (
            accountRepository.existsByAccountNumber(accountNumber)
        );

        return accountNumber;
    }

    private String generateNumber() {

        StringBuilder builder =
                new StringBuilder(ACCOUNT_NUMBER_LENGTH);

        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
            builder.append(random.nextInt(10));
        }

        return builder.toString();
    }
}