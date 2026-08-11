package com.bank.common.util;

import java.util.UUID;

public final class UuidUtils {

    private UuidUtils() {
    }

    /**
     * Generates a random UUID.
     */
    public static UUID randomUuid() {
        return UUID.randomUUID();
    }

    /**
     * Generates a random UUID as String.
     */
    public static String randomUuidString() {
        return UUID.randomUUID().toString();
    }

    /**
     * Checks whether the given string is a valid UUID.
     */
    public static boolean isValid(String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Converts String to UUID.
     */
    public static UUID fromString(String value) {
        return UUID.fromString(value);
    }
}