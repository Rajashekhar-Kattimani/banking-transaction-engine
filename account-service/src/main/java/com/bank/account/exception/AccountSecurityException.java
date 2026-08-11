package com.bank.account.exception;

public class AccountSecurityException
        extends RuntimeException {

    public AccountSecurityException(String message) {
        super(message);
    }
}