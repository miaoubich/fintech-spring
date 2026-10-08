package com.miaoubich.outbox;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.miaoubich.model.OutboxDeadLetter;
import com.miaoubich.model.OutboxEvent;
import com.miaoubich.repository.OutboxDeadLetterRepository;
import com.miaoubich.repository.OutboxEventRepository;

import jakarta.transaction.Transactional;

@Service
public class OutboxPublisherService {

	private static final Logger LOG = LoggerFactory.getLogger(OutboxPublisherService.class);
	private static final long KAFKA_ACK_TIMEOUT_SECONDS = 5;
//	private static final int MAX_RETRIES = 10;

	private final OutboxEventRepository outboxEventRepository;
	private final OutboxDeadLetterRepository outboxDeadLetterRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final String topic;
	private final String deadLetterTopic;
	private final int maxRetries;

	public OutboxPublisherService(OutboxEventRepository outboxEventRepository,
			OutboxDeadLetterRepository outboxDeadLetterRepository, KafkaTemplate<String, String> kafkaTemplate,
			@Value("${app.kafka.topics.trades:trades-events}") String topic,
			@Value("${app.kafka.topics.dlq-topic:outbox.dead-letter}") String deadLetterTopic,
			@Value("${app.kafka.topics.dlq.max-retry:10}") int maxRetries) {
		this.outboxEventRepository = outboxEventRepository;
		this.outboxDeadLetterRepository = outboxDeadLetterRepository;
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
		this.deadLetterTopic = deadLetterTopic;
		this.maxRetries = maxRetries;
	}

	@Scheduled(fixedDelayString = "${app.outbox.poll-interval:5000}", 
			   initialDelayString = "${app.outbox.initial-delay:10000}"
			   )
	@Transactional
	public void publishPendingEvents() {

		// 1. Fetch the unprocessed events from DB
		List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEventsForUpdate();

		if (pendingEvents.isEmpty()) {
			return;
		}

		LOG.info("Found {} pending outbox events", pendingEvents.size());

		for (OutboxEvent event : pendingEvents) {
			// DLQ check FIRST — skip sending if retry limit exceeded
			if (event.getRetryCount() >= maxRetries) {
				moveToDeadLetterQueue(event);
				continue; // move to next event
			}

			try {
				// 2. Dispatch the event to Kafka
				// 3. SYNCHRONOUS WAIT FOR BROKER ACK:
				// Blocks until Kafka broker cluster confirms write on
				// the leader brocker and the onsync replicat (acks=all)
				SendResult<String, String> result = kafkaTemplate
						.send(topic, event.getAggregateId(), event.getPayload())
						.get(KAFKA_ACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

				// 4. Update status ONLY AFTER broker confirmed receipt
				event.markAsPublished();
				outboxEventRepository.save(event);
				LOG.info("Published OutboxEvent id={}, type={}, aggregateId={} successfully", event.getId(),
						event.getEventType(), event.getAggregateId());
				
				result.getProducerRecord().headers().forEach(header -> LOG.debug("Kafka header: {}={}", header.key(), new String(header.value())));
				// 1. What YOU sent (ProducerRecord):
				String sentKey   = result.getProducerRecord().key();   // "T-100"
				String sentTopic = result.getProducerRecord().topic(); // "trade-events"
				String sentBody  = result.getProducerRecord().value(); // "{\"tradeId\":\"T-100\"...}"

				// 2. What KAFKA acknowledged (RecordMetadata):
				int partition    = result.getRecordMetadata().partition(); // 2
				long offset      = result.getRecordMetadata().offset();    // 1547
				long timestamp   = result.getRecordMetadata().timestamp(); // broker commit time
				
				LOG.debug("Kafka ACK: key={}, topic={}, body={}, partition={}, offset={}, timestamp={}", 
						sentKey, sentTopic,sentBody, partition, offset, timestamp);

			} catch (TimeoutException e) {
				// Broker is unresponsive or under high load
				String errorMsg = "Kafka ACK timeout after " + KAFKA_ACK_TIMEOUT_SECONDS + "s";
			    event.incrementRetry(errorMsg); // Increments counter
			    outboxEventRepository.save(event); // Persists new count and error
				
			    LOG.error(
						"Timeout ({}s) waiting for Kafka ACK on OutboxEvent [id={}, aggregateId={}]. Will retry next poll.",
						KAFKA_ACK_TIMEOUT_SECONDS, event.getId(), event.getAggregateId());
				break; // Stop loop immediately: do not flood a slow broker

			} catch (Exception e) {
				event.incrementRetry(e.getMessage());
				outboxEventRepository.save(event);
				LOG.error("Failed to publish OutboxEvent id={}, error={} -> Will retry.", event.getId(), e.getMessage(),
						e);
				// If Kafka fails, the processed remains false → scheduler retries next cycle -
				// outbox retry
				break;
			}
		}
	}

	private void moveToDeadLetterQueue(OutboxEvent event) {
		LOG.error("OutboxEvent id={}, aggregateId={} exceeded max retries ({}/{}). Moving to DLQ.", event.getId(),
				event.getAggregateId(), event.getRetryCount(), maxRetries);

		boolean publishedToDlqTopic = false;

		// 1. Publish to Kafka DLQ (Dead Letter Queue) topic for external monitors / alerting
		try {
			kafkaTemplate.send(deadLetterTopic, event.getAggregateId(), event.getPayload())
					.get(KAFKA_ACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			
			publishedToDlqTopic = true;
			
		} catch (Exception e) {
			LOG.error("Failed to publish to Kafka DLQ topic '{}' for OutboxEvent id={}: {}", deadLetterTopic,
					event.getId(), e.getMessage());
		}

		// 2. Persist to PostgreSQL outbox_dead_letter audit table
		OutboxDeadLetter dlq = new OutboxDeadLetter(
				event.getEventType(), 
				event.getAggregateId(), 
				event.getPayload(),
				event.getRetryCount(), 
				event.getLastError(), 
				Instant.now(), 
				publishedToDlqTopic // Stores whether Kafka
																								// DLQ topic received it
		);
		outboxDeadLetterRepository.save(dlq);

		// 3. Mark original outbox row as DEAD_LETTER (Never polled again, perfect audit log)
        event.markAsDeadLetter("Exceeded max retries: " + maxRetries);
		outboxEventRepository.save(event);

		LOG.warn("OutboxEvent id={} quarantined in DLQ table and marked as processed in outbox.", event.getId());
	}
}
