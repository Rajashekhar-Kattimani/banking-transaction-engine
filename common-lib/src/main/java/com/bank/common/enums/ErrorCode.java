package com.bank.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	ACCOUNT_NOT_FOUND("ACC_001", "Account not found"), 
	DUPLICATE_ACCOUNT("ACC_002", "Account already exists"),
	INSUFFICIENT_BALANCE("ACC_003", "Insufficient account balance"),
	CUSTOMER_NOT_FOUND("CUS_001", "Customer not found"), 
	INVALID_TRANSACTION("TXN_001", "Invalid transaction"),
	UNAUTHORIZED("SEC_001", "Unauthorized"), 
	FORBIDDEN("SEC_002", "Access denied"),
	VALIDATION_ERROR("GEN_001", "Validation failed"), 
	INTERNAL_SERVER_ERROR("GEN_999", "Internal server error"),
	OPTIMISTIC_LOCK_FAILED(
	        "GEN_010",
	        "The record was modified by another transaction");

	private final String code;
	private final String message;

}