package com.miaoubich.agent.dto;

public record TradeDTO(
		
		String tradeId, 
		String username, 
		String symbol, 
		String side, 
		Double amount, 
		Double price, 
		String status, 
		String createdAt) 
{}