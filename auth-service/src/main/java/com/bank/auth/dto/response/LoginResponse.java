package com.bank.auth.dto.response;

import java.time.Instant;

public record LoginResponse(

        String accessToken,

        String refreshToken,

        Instant expiresAt,

        String tokenType

) {
}