package com.bank.messaging.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KafkaProducerConfig {

	private final KafkaMessagingProperties properties;

	@Bean
	public ProducerFactory<String, Object> producerFactory() {

		Map<String, Object> config = new HashMap<>();

		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());

		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		/*
		 * Add event class information to Kafka headers.
		 *
		 * Example: TransferInitiatedEvent TransferDebitedEvent TransferCompletedEvent
		 */
		config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {

		return new KafkaTemplate<>(producerFactory);
	}
}