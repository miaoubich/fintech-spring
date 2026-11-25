package com.miaoubich.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WalletResponse(
        String userId,
        BigDecimal totalCashBalance,
        BigDecimal totalPortfolioValue,
        BigDecimal totalPnl,
        List<CashBalance> cashBalances,
        List<Position> positions
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CashBalance(
            String currency,
            BigDecimal amount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Position(
            String symbol,
            String assetClass,
            BigDecimal quantity,
            BigDecimal avgCost,
            BigDecimal currentPrice,
            BigDecimal marketValue,
            BigDecimal unrealizedPnl
    ) {}
}