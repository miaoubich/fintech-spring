package com.miaoubich.ledger.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountBalanceResponse(
		
		String userId,
        String symbol,
        BigDecimal positionQuantity,
        BigDecimal cashBalance,
        Instant updatedAt
		
) {}
