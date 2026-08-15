package com.bank.messaging.constant;

public final class KafkaTopics {

	private KafkaTopics() {
	}

	public static final String TRANSFER_INITIATED = "transfer.initiated";

	public static final String TRANSFER_DEBITED = "transfer.debited";

	public static final String TRANSFER_COMPLETED = "transfer.completed";

	public static final String TRANSFER_FAILED = "transfer.failed";
	
	public static final String TRANSFER_COMPENSATE = "transfer.compensate";

	public static final String TRANSFER_COMPENSATED = "transfer.compensated";
}