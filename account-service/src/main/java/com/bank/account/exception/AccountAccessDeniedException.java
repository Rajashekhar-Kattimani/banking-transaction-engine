package com.bank.account.exception;

public class AccountAccessDeniedException extends RuntimeException {

    public AccountAccessDeniedException() {
        super("You are not authorized to access this account");
    }
}