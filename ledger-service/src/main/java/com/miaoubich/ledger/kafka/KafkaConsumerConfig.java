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
	    var backOff = new ExponentialBackOffWithMaxRetries(5);
	    backOff.setInitialInterval(1_000);
	    backOff.setMultiplier(2.0);
	    backOff.setMaxInterval(30_000);
	    return new DefaultErrorHandler(recoverer, backOff);
	}
}
