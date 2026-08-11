package com.bank.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank.security.jwt")
public record JwtProperties(

		String issuer,

		String audience,

		String secret,

		Long accessTokenExpiration,

		Long refreshTokenExpiration

) {
}
