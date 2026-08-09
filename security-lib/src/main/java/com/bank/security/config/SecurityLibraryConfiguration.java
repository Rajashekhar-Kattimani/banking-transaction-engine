package com.bank.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.bank.security.jwt.service.JwtService;
import com.bank.security.jwt.service.JwtServiceImpl;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityLibraryConfiguration {

    @Bean
    public JwtService jwtService(JwtProperties jwtProperties) {
        return new JwtServiceImpl(jwtProperties);
    }
}