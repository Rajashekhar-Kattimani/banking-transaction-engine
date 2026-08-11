package com.bank.common.util;

import java.util.Collection;
import java.util.Objects;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static <T> T requireNonNull(T value, String message) {

        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public static String requireNotBlank(String value, String message) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public static int requirePositive(int value, String message) {

        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public static long requirePositive(long value, String message) {

        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public static double requirePositive(double value, String message) {

        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public static int requireNonNegative(int value, String message) {

        if (value < 0) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public static <T extends Collection<?>> T requireNotEmpty(T collection, String message) {

        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return collection;
    }

    public static void requireTrue(boolean expression, String message) {

        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireFalse(boolean expression, String message) {

        if (expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireState(boolean expression, String message) {

        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static String requireValidUuid(String value, String message) {

        if (!UuidUtils.isValid(value)) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public static <T> T requireEquals(T expected, T actual, String message) {

        if (!Objects.equals(expected, actual)) {
            throw new IllegalArgumentException(message);
        }

        return actual;
    }
}