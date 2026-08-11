package com.bank.common.constants;

public final class AppConstants {

	private AppConstants() {
	}

	// Headers
	public static final String CORRELATION_ID = "X-Correlation-Id";
	public static final String REQUEST_ID = "X-Request-Id";
	public static final String USER_ID = "X-User-Id";
	public static final String AUTHORIZATION = "Authorization";

	// Date Formats
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	// Kafka
	public static final String OUTBOX_TOPIC = "outbox-events";

	// Pagination
	public static final int DEFAULT_PAGE = 0;
	public static final int DEFAULT_SIZE = 20;
	public static final int MAX_PAGE_SIZE = 100;

	// Currency
	public static final String DEFAULT_CURRENCY = "USD";

	public static final String SYSTEM = "SYSTEM";

	public static final String UNKNOWN = "UNKNOWN";

	public static final String DEFAULT_TIMEZONE = "UTC";
}