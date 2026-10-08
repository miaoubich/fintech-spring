package com.miaoubich.ledger.kafka;

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
	
	@Value("${app.kafka.topics.trades:trade-events}")
	private String mainTopic;

	// 1. Automatically creates the DLT topic onstart-up (if it doesn't exist)
	@Bean
	public NewTopic tradeEventsDLTTopic(
							@Value("${app.kafka.topics.trades:trade-events}")
							String mainTopic) {
		return TopicBuilder.name(mainTopic + ".DLT") // trade-events.DLT
				.partitions(3) // Match primary topic partition count
				.replicas(1) // 1 for local dev, 3 for production
				.build();
	}
	
	@Bean
	public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
		// 1. Dead Letter Topic Recoverer
		var recoverer = new DeadLetterPublishingRecoverer(template);
		// Default convention: failed messages from "trade-events" → "trade-events.DLT"
		
		// 2. Exponential Back-off with Max Retries
		var backOff = new ExponentialBackOffWithMaxRetries(6); // 6 retries (7 attempts in total = the original attempt + 6 retries)
	    backOff.setInitialInterval(1_000); // Start with 1 second wait
	    backOff.setMultiplier(2.0); // Double the wait each time
	    backOff.setMaxInterval(30_000);// wait between retries will be capped at 30s later on 

	    var errorHandler = new DefaultErrorHandler(recoverer, backOff);

	    // 3. Non-Retryable Exceptions (poison pills → DLT immediately)
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                IllegalStateException.class
        );
        
        return errorHandler;
	}
}

/* The Exact Retry Timeline with Your Config:
 * 
 Message Fails (Initial execution)
  │
  ├── Retry 1: Wait 1,000 ms (1s)
  ├── Retry 2: Wait 2,000 ms (2s)
  ├── Retry 3: Wait 4,000 ms (4s)
  ├── Retry 4: Wait 8,000 ms (8s)
  |── Retry 5: Wait 16,000 ms (16s)
  └── Retry 5: Wait 32,000 ms (32s) Capped at 30s!
  │
  └── Total backoff time: ~63 seconds (gives DB plenty of time to recover)
  │
Exhausted ──► Sent to "trade-events.DLT" topic
 * */
