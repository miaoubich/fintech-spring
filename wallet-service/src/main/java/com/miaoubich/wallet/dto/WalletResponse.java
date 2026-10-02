package com.miaoubich.wallet.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.math.BigDecimal;
import java.util.List;

@Serdeable
public record WalletResponse(
		
    String userId,
    BigDecimal totalCashBalance,
    BigDecimal totalPortfolioValue,
    BigDecimal totalPnl, //Profit and Loss
    List<CashBalance> cashBalances,
    List<Position> positions
) {
    @Serdeable
    public record CashBalance(
        String currency,
        BigDecimal amount
    ) {}

    @Serdeable
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