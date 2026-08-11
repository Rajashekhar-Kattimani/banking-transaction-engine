package com.bank.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.bank.security.jwt.service.JwtService;
import com.bank.security.jwt.service.JwtServiceImpl;

import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityLibraryConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityLibraryConfiguration.class);

    @Bean
    public JwtService jwtService(JwtProperties jwtProperties, Environment env) {

        String secret = jwtProperties.secret();
        if (secret == null || secret.isBlank()) {
            // If running in a development/test profile, generate an ephemeral secret so the application can start.
            if (env != null && env.acceptsProfiles(Profiles.of("dev", "local", "test"))) {
                byte[] key = new byte[32]; // 256-bit key
                new SecureRandom().nextBytes(key);
                String generated = Base64.getEncoder().encodeToString(key);

                log.warn("No JWT secret configured (bank.security.jwt.secret). Using a generated ephemeral secret because active profile is dev/local/test. " +
                        "Do NOT use this in production. Configure a persistent secret in application properties or environment variables.");

                jwtProperties = new JwtProperties(
                        jwtProperties.issuer(),
                        jwtProperties.audience(),
                        generated,
                        jwtProperties.accessTokenExpiration(),
                        jwtProperties.refreshTokenExpiration());
            }
        }

        return new JwtServiceImpl(jwtProperties);
    }
}