package com.miaoubich;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private String tradesTopic;

    private String dlqTopic;
    
    public KafkaTopicConfig(
    		@Value("${app.kafka.topics.trades:trades-events}") String tradesTopic, 
    		@Value("${app.kafka.topics.dlq-topic:outbox.dead-letter}")String dlqTopic) {
		this.tradesTopic = tradesTopic;
		this.dlqTopic = dlqTopic;
    	
    }
    
    /**
     * 1. Primary Event Stream:
     * 6 Partitions allows scaling consumers from 1 to 6 threads/containers.
     */
    @Bean
    public NewTopic tradesEventsTopic() {
    	return TopicBuilder.name(tradesTopic)
				.partitions(6) // 6 partitions from Day 1
				.replicas(1) // 1 for local/Docker dev, 3 for cloud/production
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy") // Fast compression for JSON payloads
				.build();
    }
    
    /**
     * 2. Producer Dead Letter Topic:
     * Lower throughput, so 3 partitions is plenty.
     */
    @Bean
    public NewTopic outboxDeadLetterTopic() {
        return TopicBuilder.name(dlqTopic)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(30 * 24 * 60 * 60 * 1000L)) // 30 days retention
                .build();
    }
	
}
