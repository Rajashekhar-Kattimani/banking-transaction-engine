package com.bank.common.util;

import java.text.Normalizer;
import java.util.Objects;

public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public static String removeWhitespace(String value) {

        if (isBlank(value)) {
            return value;
        }

        return value.replaceAll("\\s+", "");
    }

    public static String normalize(String value) {

        if (isBlank(value)) {
            return value;
        }

        return Normalizer.normalize(value, Normalizer.Form.NFKC);
    }

    public static String capitalize(String value) {

        if (isBlank(value)) {
            return value;
        }

        return Character.toUpperCase(value.charAt(0))
                + value.substring(1).toLowerCase();
    }

    public static boolean equals(String first, String second) {
        return Objects.equals(first, second);
    }

    public static boolean equalsIgnoreCase(String first, String second) {

        if (first == null || second == null) {
            return first == second;
        }

        return first.equalsIgnoreCase(second);
    }
}