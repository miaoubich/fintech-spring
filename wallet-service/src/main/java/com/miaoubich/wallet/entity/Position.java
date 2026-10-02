package com.miaoubich.wallet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "positions",
    uniqueConstraints = {
        // Enforce 1 position entry per asset per user at DB level
        @UniqueConstraint(name = "uk_position_user_symbol", columnNames = {"user_id", "symbol"})
    },
    indexes = {
        // Fast retrieval for GET /wallet/{userId}
        @Index(name = "idx_positions_user_id", columnList = "user_id"),
        // Composite index for fast single asset lookups
        @Index(name = "idx_positions_user_symbol", columnList = "user_id, symbol")
    }
)
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "position_seq_gen")
    @SequenceGenerator(
        name = "position_seq_gen",
        sequenceName = "positions_seq",
        allocationSize = 50,
        initialValue = 1
    )
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol; // e.g., "BTC", "ETH"

    @Column(name = "asset_class", nullable = false, length = 30)
    private String assetClass; // e.g., "Crypto", "Stock"

    @Column(name = "quantity", nullable = false, precision = 38, scale = 8)
    private BigDecimal quantity;

    @Column(name = "avg_cost", nullable = false, precision = 38, scale = 8)
    private BigDecimal avgCost;

    @Column(name = "current_price", nullable = false, precision = 38, scale = 8)
    private BigDecimal currentPrice;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    public Position() {
        this.lastUpdated = Instant.now();
    }

    public Position(String userId, String symbol, String assetClass,
                    BigDecimal quantity, BigDecimal avgCost, BigDecimal currentPrice) {
        this.userId = userId;
        this.symbol = symbol;
        this.assetClass = assetClass;
        this.quantity = quantity;
        this.avgCost = avgCost;
        this.currentPrice = currentPrice;
        this.lastUpdated = Instant.now();
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = Instant.now();
    }

    public BigDecimal getMarketValue() {
        return (quantity != null && currentPrice != null) 
            ? quantity.multiply(currentPrice) 
            : BigDecimal.ZERO;
    }

    public BigDecimal getUnrealizedPnl() {
        return (quantity != null && currentPrice != null && avgCost != null) 
            ? getMarketValue().subtract(quantity.multiply(avgCost)) 
            : BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAvgCost() {
        return avgCost;
    }

    public void setAvgCost(BigDecimal avgCost) {
        this.avgCost = avgCost;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}