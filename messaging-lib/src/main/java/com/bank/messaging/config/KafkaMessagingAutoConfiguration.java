package com.bank.messaging.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import com.bank.messaging.producer.KafkaEventPublisher;

@AutoConfiguration
@EnableConfigurationProperties(KafkaMessagingProperties.class)
@Import(KafkaProducerConfig.class)
public class KafkaMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    KafkaEventPublisher kafkaEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate) {

        return new KafkaEventPublisher(kafkaTemplate);
    }
}