package com.miaoubich.ledger.dto;

import java.math.BigDecimal;
import java.time.Instant;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record AccountBalanceResponse(
		
		String userId,
        String symbol,
        BigDecimal positionQuantity,
        BigDecimal cashBalance,
        Instant updatedAt
		
) {}
