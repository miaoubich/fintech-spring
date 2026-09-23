package com.miaoubich.ledger.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "account_balances",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_symbol",
                        columnNames = {"user_id", "symbol"}
                )
        }
)
public class AccountBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_balance_seq_gen")
    @SequenceGenerator(
            name = "account_balance_seq_gen",
            sequenceName = "account_balances_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "symbol", nullable = false, length = 50)
    private String symbol;

    @Column(name = "position_quantity", nullable = false, precision = 38, scale = 18)
    private BigDecimal positionQuantity;

    @Column(name = "cash_balance", nullable = false, precision = 38, scale = 18)
    private BigDecimal cashBalance;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AccountBalance() {
    }

	public AccountBalance(String userId, String symbol) {
        this.userId = userId;
        this.symbol = symbol;
        this.positionQuantity = BigDecimal.ZERO;
        this.cashBalance = BigDecimal.ZERO;
        this.updatedAt = Instant.now();
    }

    public void applyDelta(BigDecimal positionDelta, BigDecimal cashDelta) {
        this.positionQuantity = this.positionQuantity.add(positionDelta);
        this.cashBalance = this.cashBalance.add(cashDelta);
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public BigDecimal getPositionQuantity() { return positionQuantity; }
    public BigDecimal getCashBalance() { return cashBalance; }
    public Instant getUpdatedAt() { return updatedAt; }
}