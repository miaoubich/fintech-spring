package com.miaoubich.ledger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
    name = "processed_trades",
    indexes = {
        @Index(name = "idx_processed_trades_processed_at", columnList = "processed_at ASC")
    }
)
public class ProcessedTrade {

    @Id
    @Column(name = "trade_id", length = 100, nullable = false)
    private String tradeId;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    public ProcessedTrade() {}

    public ProcessedTrade(String tradeId) {
        this.tradeId = tradeId;
        this.processedAt = Instant.now();
    }

    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}