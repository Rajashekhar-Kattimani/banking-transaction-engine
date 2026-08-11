package com.bank.persistence.specification;

import java.io.Serializable;

public record SearchCriteria(

		String field,

		SearchOperation operation,

		Object value

) implements Serializable {

	private static final long serialVersionUID = 1L;

}