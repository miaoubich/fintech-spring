package com.miaoubich.agent.dto;

public record LedgerEntry(
		
		String entryId, 
		String username, 
		String type, 
		Double amount, 
		String currency, 
		String createdAt) 
{}