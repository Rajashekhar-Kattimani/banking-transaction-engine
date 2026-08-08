package com.bank.transaction.exception;

public class AccountOperationException
        extends RuntimeException {

    public AccountOperationException(String message) {
        super(message);
    }
}