package com.miaoubich.dto;

import com.miaoubich.model.Trade;
import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;

@Serdeable
public record TradeResponse(
        String tradeId,
        String userId,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        String status,
        String asset,
        Instant createdAt,
        Instant updatedAt
) {
    public static TradeResponse from(Trade trade) {
        return new TradeResponse(
                trade.getTradeId(),
                trade.getUserId(),
                trade.getSymbol(),
                trade.getSide(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getStatus(),
                trade.getAsset(),
                trade.getCreatedAt(),
                trade.getUpdatedAt()
        );
    }
}