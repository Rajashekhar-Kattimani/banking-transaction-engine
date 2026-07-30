package com.bank.common.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(

		boolean success,

		String errorCode,

		String message,

		List<String> errors,

		OffsetDateTime timestamp

) implements BaseResponse {

	public static ErrorResponse of(String errorCode, String message, List<String> errors) {

		return new ErrorResponse(false, errorCode, message, errors, OffsetDateTime.now());
	}

	public static ErrorResponse of(String errorCode, String message) {

		return new ErrorResponse(false, errorCode, message, List.of(), OffsetDateTime.now());
	}
}