package com.miaoubich.ledger.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConsumerConfig {

	private static final int PARTITIONS = 3;
	
	@Bean
	public NewTopic tradeEventsTopic(
	        @Value("${app.kafka.topics.trades:trade-events}") String mainTopic) {
	    return TopicBuilder.name(mainTopic)
	            .partitions(PARTITIONS)
	            .replicas(1)
	            .build();
	}
	
	@Bean
	public NewTopic tradeEventsDLTTopic(
							@Value("${app.kafka.topics.trades:trade-events}")
							String mainTopic) {
		return TopicBuilder.name(mainTopic + ".DLT")
				.partitions(PARTITIONS)
				.replicas(1)
				.build();
	}

	@Bean
	public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
		// 1. Dead Letter Topic Recoverer
		var recoverer = new DeadLetterPublishingRecoverer(template);
		// Default convention: failed messages from "trade-events" → "trade-events.DLT"
		
		// 2. Exponential Backoff with Max Retries
		var backOff = new ExponentialBackOffWithMaxRetries(6);
	    backOff.setInitialInterval(1_000); // Start with 1 second wait
	    backOff.setMultiplier(2.0); // Double the wait each time
	    backOff.setMaxInterval(30_000); // Cap at 30 seconds max
	    
	    var errorHandler = new DefaultErrorHandler(recoverer, backOff);
	    
	 // 3. Non-Retryable Exceptions (poison pills → DLT immediately)
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                IllegalStateException.class
        );
        return errorHandler;
	}
}
