package com.bank.common.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private DateTimeUtils() {
	}

	public static LocalDateTime nowUtc() {
		return LocalDateTime.now(ZoneOffset.UTC);
	}

	public static String format(LocalDateTime dateTime) {

		if (dateTime == null) {
			return null;
		}

		return DATE_TIME_FORMATTER.format(dateTime);
	}

	public static LocalDateTime parse(String value) {

		if (value == null || value.isBlank()) {
			return null;
		}

		return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
	}

	public static boolean isPast(LocalDateTime dateTime) {

		return dateTime != null && dateTime.isBefore(nowUtc());
	}

	public static boolean isFuture(LocalDateTime dateTime) {

		return dateTime != null && dateTime.isAfter(nowUtc());
	}
}