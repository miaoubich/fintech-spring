package com.miaoubich.ledger.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntryResponse(
        String tradeId,
        String userId,
        String symbol,
        String side,
        String asset,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal cashAmount,
        String status,
        Instant createdAt
) {}
