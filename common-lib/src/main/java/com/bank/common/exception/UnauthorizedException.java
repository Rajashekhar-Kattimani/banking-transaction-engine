package com.bank.common.exception;

import com.bank.common.enums.ErrorCode;

public final class UnauthorizedException extends BankingException {

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public UnauthorizedException(
            ErrorCode errorCode,
            String message,
            Throwable cause) {
        super(errorCode, message, cause);
    }
}