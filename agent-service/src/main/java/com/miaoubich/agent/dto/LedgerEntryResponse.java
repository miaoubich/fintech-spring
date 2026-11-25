package com.miaoubich.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
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