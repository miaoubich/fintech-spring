package com.miaoubich.ledger.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;

@Serdeable
public record TradeEvent(
        
		String tradeId,
        String userId,
        String symbol,
        String side,
        String asset,
        BigDecimal quantity,
        BigDecimal price,
        String status,
        Instant timestamp
) {
}