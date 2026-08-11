package com.bank.gateway.filter;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class GatewayGlobalFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(
            org.springframework.web.server.ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String correlationId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(CORRELATION_ID);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .header(CORRELATION_ID, correlationId)
                        .build();

        return chain.filter(
                exchange.mutate()
                        .request(request)
                        .build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}