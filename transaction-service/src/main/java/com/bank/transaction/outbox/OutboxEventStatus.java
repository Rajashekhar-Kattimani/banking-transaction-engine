package com.bank.transaction.outbox;

public enum OutboxEventStatus {

    PENDING,

    PUBLISHED,

    FAILED
}