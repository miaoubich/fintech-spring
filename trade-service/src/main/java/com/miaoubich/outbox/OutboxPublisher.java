package com.miaoubich.outbox;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;

import com.miaoubich.model.OutboxEvent;
import com.miaoubich.repository.OutboxEventRepository;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

@Singleton
public class OutboxPublisher {

	private static final Logger LOG = LoggerFactory.getLogger(OutboxPublisher.class);
	private static final long KAFKA_ACK_TIMEOUT_SECONDS = 5;

	private final OutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final String topic;

	public OutboxPublisher(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate,
			@Value("${app.kafka.topics.trades:trades-events}") String topic) {
		this.outboxEventRepository = outboxEventRepository;
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	@Scheduled(
			fixedDelayString = "${app.outbox.poll-interval:5000}", 
			initialDelayString = "${app.outbox.initial-delay:10000}"
	)
	@Transactional
	public void publishPendingEvents() {

		// 1. Fetch the unprocessed events from DB
		List<OutboxEvent> pendingEvents = outboxEventRepository.findUnprocessedEventsForUpdate();

		if (pendingEvents.isEmpty()) {
			return;
		}

		LOG.info("Found {} pending outbox events", pendingEvents.size());

		for (OutboxEvent event : pendingEvents) {
			try {
				// 2. Dispatch the event to Kafka
				CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());
				
				// 3. SYNCHRONOUS WAIT FOR BROKER ACK:
                // Blocks until Kafka broker cluster confirms write on 
				//  the leader brocker and the onsync replicat (acks=all)
                SendResult<String, String> result = future.get(KAFKA_ACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                // 4. Update status ONLY AFTER broker confirmed receipt
				event.markAsProcessed();
				outboxEventRepository.save(event);
				LOG.info("Published OutboxEvent id={}, type={}, aggregateId={} successfully", event.getId(),
						event.getEventType(), event.getAggregateId());

			} 
			catch (TimeoutException e) {
                // Broker is unresponsive or under high load
                LOG.error("Timeout ({}s) waiting for Kafka ACK on OutboxEvent [id={}, aggregateId={}]. Will retry next poll.",
                        KAFKA_ACK_TIMEOUT_SECONDS, event.getId(), event.getAggregateId());
                break; // Stop loop immediately: do not flood a slow broker

            }
			catch (Exception e) {
				LOG.error("Failed to publish OutboxEvent id={}, error={} -> Will retry.", event.getId(), e.getMessage(),
						e);
				// If Kafka fails, the processed remains false → scheduler retries next cycle
				break;
			}
		}
	}
}
