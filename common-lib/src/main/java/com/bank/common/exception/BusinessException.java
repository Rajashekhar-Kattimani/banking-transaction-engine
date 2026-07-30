package com.bank.common.exception;

import com.bank.common.enums.ErrorCode;

public final class BusinessException extends BankingException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(
            ErrorCode errorCode,
            String message,
            Throwable cause) {
        super(errorCode, message, cause);
    }
}