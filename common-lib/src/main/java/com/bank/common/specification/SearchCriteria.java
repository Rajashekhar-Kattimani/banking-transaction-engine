package com.bank.common.specification;

public record SearchCriteria(

		String field,

		SearchOperation operation,

		Object value

) {
}