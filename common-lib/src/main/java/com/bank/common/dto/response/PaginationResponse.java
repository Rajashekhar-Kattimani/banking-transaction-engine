package com.bank.common.dto.response;

import java.util.List;

public record PaginationResponse<T>(

		List<T> content,

		int page,

		int size,

		long totalElements,

		int totalPages,

		boolean first,

		boolean last,

		boolean empty

) {
}