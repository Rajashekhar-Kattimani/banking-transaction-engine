package com.bank.common.exception;

import com.bank.common.enums.ErrorCode;

public final class CacheException extends BankingException {

	public CacheException(String message) {
		super(ErrorCode.INTERNAL_SERVER_ERROR, message);
	}

	public CacheException(String message, Throwable cause) {
		super(ErrorCode.INTERNAL_SERVER_ERROR, message, cause);
	}
}
