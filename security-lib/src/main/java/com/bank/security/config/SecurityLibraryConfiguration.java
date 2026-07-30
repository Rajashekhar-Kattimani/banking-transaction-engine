package com.bank.security.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bank.security.jwt.service.JwtService;
import com.bank.security.jwt.service.JwtServiceImpl;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityLibraryConfiguration {

    @Bean
    JwtService jwtService(JwtProperties jwtProperties) {
        return new JwtServiceImpl(jwtProperties);
    }
}