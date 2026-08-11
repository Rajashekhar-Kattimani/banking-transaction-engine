package com.bank.persistence.specification;

public enum SearchOperation {

    EQUAL,
    NOT_EQUAL,

    GREATER_THAN,
    GREATER_THAN_EQUAL,

    LESS_THAN,
    LESS_THAN_EQUAL,

    LIKE,
    STARTS_WITH,
    ENDS_WITH,
    CONTAINS,

    IN,
    NOT_IN,

    BETWEEN,

    IS_NULL,
    IS_NOT_NULL,

    TRUE,
    FALSE
}