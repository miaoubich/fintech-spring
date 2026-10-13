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

	@Bean
	public NewTopic tradeEventsDLTTopic(
							@Value("${app.kafka.topics.trades:trade-events}")
							String mainTopic) {
		return TopicBuilder.name(mainTopic + ".DLT")
				.partitions(3)
				.replicas(1)
				.build();
	}

	@Bean
	public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
		var recoverer = new DeadLetterPublishingRecoverer(template);
		var backOff = new ExponentialBackOffWithMaxRetries(6);
	    backOff.setInitialInterval(1_000);
	    backOff.setMultiplier(2.0);
	    backOff.setMaxInterval(30_000);
	    var errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                IllegalStateException.class
        );
        return errorHandler;
	}
}
