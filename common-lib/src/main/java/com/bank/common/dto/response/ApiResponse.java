package com.bank.common.dto.response;

import java.time.OffsetDateTime;

public record ApiResponse<T>(boolean success, String message, T data, OffsetDateTime timestamp)
		implements BaseResponse {

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, message, data, OffsetDateTime.now());
	}

	public static <T> ApiResponse<T> success(T data) {
		return success("Success", data);
	}

	public static ApiResponse<Void> success(String message) {
		return new ApiResponse<>(true, message, null, OffsetDateTime.now());
	}
}