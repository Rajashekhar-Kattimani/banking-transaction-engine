package com.bank.account.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

	private static final String BOOTSTRAP_SERVERS = "localhost:9092";

	private static final String GROUP_ID = "account-service";

	@Bean
	ConsumerFactory<String, Object> consumerFactory() {

		Map<String, Object> properties = new HashMap<>();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

		properties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);

		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

		JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();

		/*
		 * Trust only the event package from messaging-lib.
		 */
		jsonDeserializer.addTrustedPackages("com.bank.messaging.event");

		/*
		 * The producer JsonSerializer adds the Java type information in Kafka headers.
		 *
		 * Therefore the consumer can deserialize:
		 *
		 * TransferInitiatedEvent TransferDebitedEvent TransferFailedEvent
		 *
		 * correctly.
		 */
		jsonDeserializer.setUseTypeHeaders(true);

		return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), jsonDeserializer);
	}

	@Bean(name = "kafkaListenerContainerFactory")
	ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
			ConsumerFactory<String, Object> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory);

		return factory;
	}
}