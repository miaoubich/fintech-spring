package com.miaoubich.agent.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TradeResponse(
		
		String tradeId, 
		String userId, 
		String symbol, 
		String side, // BUY / SELL
		Double quantity, 
		Double price, 
		String status, // PENDING, FILLED, REJECTED, CANCELLED
		String asset,
		Instant createdAt,
		Instant updatedAt
) {}