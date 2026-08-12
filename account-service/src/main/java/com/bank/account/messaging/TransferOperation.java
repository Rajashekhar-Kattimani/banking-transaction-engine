package com.bank.account.messaging;

public final class TransferOperation {

    private TransferOperation() {
    }

    public static final String DEBIT = "DEBIT";

    public static final String CREDIT = "CREDIT";

    public static final String COMPENSATION = "COMPENSATION";
}