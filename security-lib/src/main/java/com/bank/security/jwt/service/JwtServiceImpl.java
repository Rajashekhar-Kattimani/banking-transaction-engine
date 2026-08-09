package com.bank.security.jwt.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bank.security.config.JwtProperties;
import com.bank.security.constants.JwtClaims;
import com.bank.security.user.UserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtServiceImpl(JwtProperties jwtProperties) {

        this.jwtProperties = jwtProperties;

        String secret = jwtProperties.secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Missing required configuration property 'bank.security.jwt.secret'. " +
                    "Please set it in application.properties, application.yml or provide it via environment variables.");
        }

        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(UserPrincipal principal) {

        Map<String, Object> claims = Map.of(

                JwtClaims.ROLES,
                principal.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList(),

                JwtClaims.TOKEN_TYPE,
                "ACCESS"

        );

        return generateToken(
                principal,
                claims,
                jwtProperties.accessTokenExpiration());
    }

    @Override
    public String generateRefreshToken(UserPrincipal principal) {

        Map<String, Object> claims = Map.of(

                JwtClaims.TOKEN_TYPE,
                "REFRESH"

        );

        return generateToken(
                principal,
                claims,
                jwtProperties.refreshTokenExpiration());
    }

    @Override
    public String generateToken(
            UserPrincipal principal,
            Map<String, Object> claims,
            long expiration) {

        Instant now = Instant.now();

        Instant expiry = now.plusMillis(expiration);

        return Jwts.builder()

                .claims(claims)

                .subject(principal.getUsername())

                .issuer(jwtProperties.issuer())

                .audience()
                .add(jwtProperties.audience())
                .and()

                .id(UUID.randomUUID().toString())

                .issuedAt(Date.from(now))

                .expiration(Date.from(expiry))

                .signWith(signingKey)

                .compact();
    }

    @Override
    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Instant extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration)
                .toInstant();
    }

    @Override
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        Claims claims = extractClaims(token);

        return claimsResolver.apply(claims);
    }

    @Override
    public boolean validateToken(
            String token,
            UserDetails userDetails) {

        String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .isBefore(Instant.now());
    }

    private Claims extractClaims(String token) {

        return Jwts.parser()

                .verifyWith(signingKey)

                .build()

                .parseSignedClaims(token)

                .getPayload();
    }
}