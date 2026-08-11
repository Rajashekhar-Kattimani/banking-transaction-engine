package com.bank.common.exception;

import com.bank.common.enums.ErrorCode;

import lombok.Getter;

@Getter
public sealed class BankingException extends RuntimeException
        permits BusinessException,
                ValidationException,
                ResourceNotFoundException,
                DuplicateResourceException,
                UnauthorizedException,
                ForbiddenException,
                OptimisticLockingFailureException,
                CacheException {

    private final ErrorCode errorCode;

    protected BankingException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected BankingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BankingException(
            ErrorCode errorCode,
            String message,
            Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}