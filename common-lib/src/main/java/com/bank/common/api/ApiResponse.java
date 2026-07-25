package com.bank.common.api;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	private boolean success;

	private String message;

	private T data;

	@Builder.Default
	private Instant timestamp = Instant.now();

	private String path;
}