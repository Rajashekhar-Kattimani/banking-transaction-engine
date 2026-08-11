package com.bank.account.dto.response;

import java.math.BigDecimal;

public record AccountBalanceResponse(

        String accountNumber,

        BigDecimal balance
) {
}