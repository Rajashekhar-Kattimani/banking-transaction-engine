package com.bank.security.jwt;

import java.security.PrivateKey;
import java.security.PublicKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtTokenProvider(
            JwtProperties jwtProperties,
            KeyLoader keyLoader) {

        this.jwtProperties = jwtProperties;
        this.privateKey = keyLoader.loadPrivateKey();
        this.publicKey = keyLoader.loadPublicKey();
    }
    
    private Claims parseClaims(String token) {

        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
    
    public boolean validateToken(String token) {

        try {

            parseClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException ex) {

            return false;

        }

    }
    
    public String getUsername(String token) {

        return parseClaims(token).getSubject();

    }
    
    public Long getUserId(String token) {

        return parseClaims(token)
                .get(JwtClaims.USER_ID, Long.class);

    }
    
    public String getEmail(String token) {

        return parseClaims(token)
                .get(JwtClaims.EMAIL, String.class);

    }

}