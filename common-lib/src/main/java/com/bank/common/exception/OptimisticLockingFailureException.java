package com.bank.common.exception;

import com.bank.common.enums.ErrorCode;

public final class OptimisticLockingFailureException extends BankingException {

    public OptimisticLockingFailureException() {
        super(
                ErrorCode.OPTIMISTIC_LOCK_FAILED,
                ErrorCode.OPTIMISTIC_LOCK_FAILED.getMessage());
    }

    public OptimisticLockingFailureException(String message) {
        super(
                ErrorCode.OPTIMISTIC_LOCK_FAILED,
                message);
    }

    public OptimisticLockingFailureException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.OPTIMISTIC_LOCK_FAILED,
                message,
                cause);
    }
}