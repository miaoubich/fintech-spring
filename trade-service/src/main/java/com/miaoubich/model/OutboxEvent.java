package com.miaoubich.model;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_events_processed", columnList = "processed, created_at"),
        @Index(name = "idx_outbox_events_aggregate", columnList = "aggregate_id")
})
public class OutboxEvent {

	
	public enum Status {
        PENDING,       // Waiting to be picked up
        PUBLISHED,     // Successfully sent to primary Kafka topic
        DEAD_LETTER    // Aborted after max retries and moved to DLQ
    }
	
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outbox_event_seq_gen")
    @SequenceGenerator(
            name = "outbox_event_seq_gen",
            sequenceName = "outbox_events_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    @Column(nullable = false)
    private int retryCount = 0;

    private String lastError;

    public OutboxEvent() {
    }

    public OutboxEvent(String eventType, String aggregateId, String aggregateType, String payload) {
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    // State Transition Helpers
    public void markAsPublished() {
        this.status = Status.PUBLISHED;
    }
    
    public void markAsDeadLetter(String reason) {
        this.status = Status.DEAD_LETTER;
        this.lastError = reason;
    }

    public void incrementRetry(String errorMessage) {
        this.retryCount++;
        this.lastError = errorMessage;
    }
    
	public int getRetryCount() {
		return retryCount;
	}

	public String getLastError() {
		return lastError;
	}

	public Status getStatus() {
		return status;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public Long getId() {
		return id;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(String aggregateId) {
		this.aggregateId = aggregateId;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
