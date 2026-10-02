package com.miaoubich.wallet.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "cash_balances",
    uniqueConstraints = {
        // Enforce 1 cash balance per user per currency at DB level
        @UniqueConstraint(name = "uk_cash_user_currency", columnNames = {"user_id", "currency"})
    },
    indexes = {
        // Fast retrieval for GET /wallet/{userId}
        @Index(name = "idx_cash_balances_user_id", columnList = "user_id"),
        // Composite index for fast updates on single currency
        @Index(name = "idx_cash_user_currency", columnList = "user_id, currency")
    }
)
public class CashBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cash_balance_seq_gen")
    @SequenceGenerator(
        name = "cash_balance_seq_gen",
        sequenceName = "cash_balances_seq",
        allocationSize = 50,
        initialValue = 1
    )
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency; // e.g., "EUR", "USD"

    @Column(name = "amount", nullable = false, precision = 38, scale = 8)
    private BigDecimal amount;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    public CashBalance() {
        this.lastUpdated = Instant.now();
    }

    public CashBalance(String userId, String currency, BigDecimal amount) {
        this.userId = userId;
        this.currency = currency;
        this.amount = amount;
        this.lastUpdated = Instant.now();
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = Instant.now();
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}