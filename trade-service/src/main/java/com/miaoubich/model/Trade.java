package com.miaoubich.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "trades")
public class Trade {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_seq_gen")
	@SequenceGenerator(name = "trade_seq_gen", sequenceName = "trtade_seqs", allocationSize = 50)
	private Long id;

	@Column(name = "trade_id", nullable = false, unique = true)
	private String tradeId;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "symbol", nullable = false)
	private String symbol;

	@Column(name = "side", nullable = false)
	private String side;

	@Column(name = "quantity", nullable = false, precision = 38, scale = 18)
	private BigDecimal quantity;

	@Column(name = "price", nullable = false, precision = 38, scale = 18)
	private BigDecimal price;

	@Column(name = "status", nullable = false)
	private String status;
	
	@Column(name = "asset", nullable = false)
	private String asset;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public Trade() {
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setSide(String side) {
		this.side = side;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Trade(String tradeId, String userId, String symbol, String side, BigDecimal quantity, BigDecimal price,
			String status, String asset, Instant createdAt) {
		this.tradeId = tradeId;
		this.userId = userId;
		this.symbol = symbol;
		this.side = side;
		this.quantity = quantity;
		this.price = price;
		this.status = status;
		this.asset = asset;
		this.createdAt = createdAt;
	}
	
	@PrePersist
	public void onCreate() {
	    Instant now = Instant.now();
	    if (this.createdAt == null) {
	        this.createdAt = now;
	    }
	    this.updatedAt = now;
	}

	@PreUpdate
	public void onUpdate() {
	    this.updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}
	
	public void setTradeId(String tradeId) {
		this.tradeId = tradeId;
	}
	
	public String getTradeId() {
		return tradeId;
	}

	public String getUserId() {
		return userId;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getSide() {
		return side;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
	    this.status = status;
	    this.updatedAt = Instant.now();
	}
	
	public String getAsset() {
	    return asset;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
	
	public Instant getUpdatedAt() {
	    return updatedAt;
	}

}