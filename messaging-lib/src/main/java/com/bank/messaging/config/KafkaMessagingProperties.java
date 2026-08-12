package com.bank.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "bank.messaging.kafka")
public class KafkaMessagingProperties {

    private String bootstrapServers = "localhost:9092";
}