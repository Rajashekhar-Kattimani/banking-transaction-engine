package com.bank.common.dto.response;

public sealed interface BaseResponse permits ApiResponse, ErrorResponse {
}
