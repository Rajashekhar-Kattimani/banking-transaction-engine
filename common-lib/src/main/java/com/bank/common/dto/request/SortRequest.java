package com.bank.common.dto.request;

import com.bank.common.enums.SortDirection;

public record SortRequest(

		String sortBy,

		SortDirection direction

) {

	public static SortRequest defaultSort() {
		return new SortRequest("createdAt", SortDirection.DESC);
	}
}