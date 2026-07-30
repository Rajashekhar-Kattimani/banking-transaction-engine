package com.bank.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank.cache.redis")
public record RedisProperties(

        String host,

        Integer port,

        String username,

        String password,

        Integer database,

        Integer timeout,

        Boolean ssl

) {
}