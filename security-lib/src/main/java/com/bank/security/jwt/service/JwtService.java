package com.bank.security.jwt.service;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

import com.bank.security.user.UserPrincipal;

import io.jsonwebtoken.Claims;

public interface JwtService {

    String generateAccessToken(UserPrincipal principal);

    String generateRefreshToken(UserPrincipal principal);

    String generateToken(
            UserPrincipal principal,
            Map<String, Object> claims,
            long expiration);

    String extractUsername(String token);

    Instant extractExpiration(String token);

    <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver);

    boolean validateToken(
            String token,
            UserDetails userDetails);
}