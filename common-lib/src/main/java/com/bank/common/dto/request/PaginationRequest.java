package com.bank.common.dto.request;

import com.bank.common.enums.SortDirection;

public record PaginationRequest(

        int page,

        int size,

        String sortBy,

        SortDirection sortDirection

) {

    public PaginationRequest {

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 10;
        }

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id";
        }

        if (sortDirection == null) {
            sortDirection = SortDirection.ASC;
        }
    }
}