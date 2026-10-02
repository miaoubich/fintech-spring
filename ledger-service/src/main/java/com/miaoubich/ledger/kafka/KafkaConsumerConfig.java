package com.miaoubich.ledger.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConsumerConfig {

	@Bean
	public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
	    var recoverer = new DeadLetterPublishingRecoverer(template);
	    var backOff = new ExponentialBackOffWithMaxRetries(6); // 6 retries (7 attempts in total)
	    backOff.setInitialInterval(1_000);
	    backOff.setMultiplier(2.0);
	    backOff.setMaxInterval(30_000);// wait between retries will be capped at 30s later on 

	    var errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Skip 31s wait for deterministic client errors / poison pills
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
Exhausted ──► Sent to "trades-events.DLT" topic
 * */
