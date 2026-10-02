package com.miaoubich.wallet.kafka;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaConsumerConfig {

	private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerConfig.class);
	
	@Bean
	public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
		// 1. Recover: If all retries fail, send a message to a trade-events.DLT (Dead Letter Topic)
		var recoverer = new DeadLetterPublishingRecoverer(
				kafkaTemplate, (record, ex) -> {
					LOG.error("Message exhausted all retires. Routing to DLT topic: {}.DTL, key={}",
							record.topic(), record.key());
					return new TopicPartition(record.topic() + ".DLT", record.partition());
				});
		
		// 2. Backoff: Retry 3 times with exponential backoff
		// Attempt1: 1 second, 
		// Attempt2: 2 seconds (multiply by 2.0), 
		// Attempt3: 4 seconds
		// Max 3 retries (4 attempts in total)
		ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
		backOff.setMaxElapsedTime(7000L); // 7 seconds max
		
		var errorHandler = new DefaultErrorHandler(recoverer, backOff);
		
		// Optional: Do not retry on unrecoverable errors (e.g. malformed JSON/null pointers)
		errorHandler.addNotRetryableExceptions(IllegalArgumentException.class,
													NullPointerException.class);
		return errorHandler;
	}
}
