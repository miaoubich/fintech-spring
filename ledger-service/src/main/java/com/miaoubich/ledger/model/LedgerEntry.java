package com.miaoubich.ledger.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ledger_entry_seq_gen")
    @SequenceGenerator(
            name = "ledger_entry_seq_gen",
            sequenceName = "ledger_entries_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(name = "trade_id", nullable = false, unique = true, length = 100)
    private String tradeId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "symbol", nullable = false, length = 50)
    private String symbol;

    @Column(name = "side", nullable = false, length = 10)
    private String side;

    @Column(name = "asset", length = 50)
    private String asset;

    @Column(name = "quantity", nullable = false, precision = 38, scale = 18)
    private BigDecimal quantity;

    @Column(name = "price", nullable = false, precision = 38, scale = 18)
    private BigDecimal price;

    @Column(name = "cash_amount", nullable = false, precision = 38, scale = 18)
    private BigDecimal cashAmount;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public LedgerEntry() {
    }

    public LedgerEntry(
            String tradeId,
            String userId,
            String symbol,
            String side,
            String asset,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal cashAmount,
            String status
    ) {
        this.tradeId = tradeId;
        this.userId = userId;
        this.symbol = symbol;
        this.side = side;
        this.asset = asset;
        this.quantity = quantity;
        this.price = price;
        this.cashAmount = cashAmount;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTradeId() { return tradeId; }
    public String getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public String getSide() { return side; }
    public String getAsset() { return asset; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getCashAmount() { return cashAmount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}