package com.bank.gateway.filter;

import java.util.List;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.bank.security.jwt.service.JwtService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh"
    );

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        // Allow public authentication endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authorizationHeader =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(HttpHeaders.AUTHORIZATION);

        // Missing Authorization header
        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {

            return unauthorized(exchange);
        }

        String token =
                authorizationHeader.substring(BEARER_PREFIX.length());

        try {

            String username =
                    jwtService.extractUsername(token);

            if (username == null || username.isBlank()) {
                return unauthorized(exchange);
            }

            /*
             * Gateway validates:
             * - JWT signature
             * - JWT structure
             * - JWT expiration
             * - issuer/audience through parser configuration
             */
            jwtService.extractExpiration(token);

            /*
             * Forward authenticated identity to downstream services.
             */
            ServerWebExchange mutatedExchange =
                    exchange.mutate()
                            .request(request -> request
                                    .header(
                                            "X-Authenticated-User",
                                            username))
                            .build();

            return chain.filter(mutatedExchange);

        } catch (Exception ex) {

            return unauthorized(exchange);
        }
    }

    private boolean isPublicPath(String path) {

        return PUBLIC_PATHS.stream()
                .anyMatch(path::equals);
    }

    private Mono<Void> unauthorized(
            ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        return exchange.getResponse()
                .setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}