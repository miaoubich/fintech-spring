package com.miaoubich.wallet.dto;

import java.math.BigDecimal;
import java.util.List;


public record WalletResponse(
		
    String userId,
    BigDecimal totalCashBalance,
    BigDecimal totalPortfolioValue,
    BigDecimal totalPnl, //Profit and Loss
    List<CashBalance> cashBalances,
    List<Position> positions
) {
    public record CashBalance(
        String currency,
        BigDecimal amount
    ) {}

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