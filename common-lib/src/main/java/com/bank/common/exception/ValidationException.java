package com.bank.common.exception;

import com.bank.common.enums.ErrorCode;

public final class ValidationException extends BankingException {

    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ValidationException(
            ErrorCode errorCode,
            String message,
            Throwable cause) {
        super(errorCode, message, cause);
    }
}