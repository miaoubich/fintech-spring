package com.miaoubich.agent.dto;

public record WalletBalance(
		
		String username, 
		String currency, 
		Double available, 
		Double locked) 
{}