package com.bank.account.messaging;

import java.time.Instant;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bank.account.entity.ProcessedTransfer;
import com.bank.account.repository.ProcessedTransferRepository;
import com.bank.account.service.AccountService;
import com.bank.messaging.constant.KafkaTopics;
import com.bank.messaging.event.TransferCompensationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.bank.messaging.event.TransferCompensatedEvent;
import com.bank.messaging.producer.KafkaEventPublisher;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferCompensationListener {

	private static final String OPERATION_COMPENSATION = "COMPENSATION";

	private final AccountService accountService;
	private final ProcessedTransferRepository processedTransferRepository;
	private final KafkaEventPublisher eventPublisher;

	@KafkaListener(topics = KafkaTopics.TRANSFER_COMPENSATE, groupId = "account-compensation-service", containerFactory = "kafkaListenerContainerFactory")
	@Transactional
	public void handle(TransferCompensationEvent event) {

		log.info("Received transfer.compensate: transactionId={}, account={}, amount={}, reason={}",
				event.transactionId(), event.accountNumber(), event.amount(), event.reason());

		if (processedTransferRepository.existsByTransactionIdAndOperation(event.transactionId(),
				OPERATION_COMPENSATION)) {

			log.info("Duplicate compensation ignored: transactionId={}", event.transactionId());

			return;
		}

		try {

			log.info("Compensating source account: account={}, amount={}", event.accountNumber(), event.amount());

			accountService.creditForTransfer(event.accountNumber(), event.amount());

			log.info("Compensation credit successful: account={}, transactionId={}", event.accountNumber(),
					event.transactionId());

			ProcessedTransfer processedTransfer = new ProcessedTransfer(UUID.randomUUID(), event.transactionId(),
					OPERATION_COMPENSATION, Instant.now());

			processedTransferRepository.save(processedTransfer);

			TransferCompensatedEvent compensatedEvent =
			        new TransferCompensatedEvent(
			                UUID.randomUUID(),
			                event.transactionId(),
			                event.transactionReference(),
			                event.accountNumber(),
			                event.amount(),
			                event.reason(),
			                Instant.now()
			        );

			log.info(
			        "Publishing transfer.compensated: transactionId={}",
			        event.transactionId()
			);

			eventPublisher.publish(
			        KafkaTopics.TRANSFER_COMPENSATED,
			        event.transactionId().toString(),
			        compensatedEvent
			);

			log.info(
			        "transfer.compensated published successfully: transactionId={}",
			        event.transactionId()
			);

		} catch (Exception ex) {

			log.error("COMPENSATION FAILED: transactionId={}, account={}, reason={}", event.transactionId(),
					event.accountNumber(), ex.getMessage(), ex);

			/*
			 * Throw the exception so Kafka can retry the message. Do not mark the
			 * compensation as processed.
			 */
			throw ex;
		}
	}
}