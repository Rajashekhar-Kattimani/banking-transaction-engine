package com.bank.common.exception;

import com.bank.common.enums.ErrorCode;

public final class ForbiddenException extends BankingException {

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ForbiddenException(
            ErrorCode errorCode,
            String message,
            Throwable cause) {
        super(errorCode, message, cause);
    }
}