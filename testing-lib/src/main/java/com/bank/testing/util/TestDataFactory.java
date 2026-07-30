package com.bank.testing.util;

import java.math.BigDecimal;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static String randomTransactionId() {
        return UUID.randomUUID().toString();
    }

    public static String accountNumber() {
        return "1000000001";
    }

    public static BigDecimal amount() {
        return BigDecimal.valueOf(1000);
    }
}