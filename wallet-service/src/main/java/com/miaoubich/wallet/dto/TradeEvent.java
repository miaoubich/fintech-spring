package com.miaoubich.wallet.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.math.BigDecimal;
import java.time.Instant;

@Serdeable
public record TradeEvent(
        String tradeId,
        String userId,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        String asset,
        String status,
        Instant timestamp
) {}