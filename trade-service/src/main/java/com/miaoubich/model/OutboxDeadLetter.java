package com.miaoubich.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "outbox_dead_letter",
    indexes = {
        @Index(name = "idx_outbox_dlq_aggregate_id", columnList = "aggregate_id"),
        @Index(name = "idx_outbox_dlq_created_at", columnList = "created_at DESC")
    }
)
public class OutboxDeadLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outbox_dlq_seq_gen")
    @SequenceGenerator(name = "outbox_dlq_seq_gen", sequenceName = "outbox_dead_letter_seq", allocationSize = 50)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    
    // Explicitly tracks if published to the Kafka dead-letter topic
    @Column(name = "published_to_kafka", nullable = false)
    private boolean publishedToKafka = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public OutboxDeadLetter() {}

    public OutboxDeadLetter(String eventType, String aggregateId, String payload,
                            Integer retryCount, String lastError, Instant createdAt, boolean publishedToKafka) {
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.publishedToKafka = publishedToKafka;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public boolean isPublishedToKafka() { return publishedToKafka; }
    public void setPublishedToKafka(boolean publishedToKafka) { this.publishedToKafka = publishedToKafka; }
    public Instant getCreatedAt() { return createdAt; }
}