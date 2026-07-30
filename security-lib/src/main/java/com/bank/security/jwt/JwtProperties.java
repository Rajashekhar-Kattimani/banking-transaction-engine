package com.bank.security.jwt;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank.security.jwt")
public record JwtProperties(

        String secret,

        Duration accessTokenValidity,

        Duration refreshTokenValidity,

        String issuer

) {
}