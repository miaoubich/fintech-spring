package com.miaoubich.wallet.dto;

import java.math.BigDecimal;

public record WalletSummaryResponse(
		
    String userId,
    BigDecimal totalPortfolioValue,
    BigDecimal totalCashValue,
    BigDecimal totalUnrealizedPnl, //Profit or Loss on positions you still hold
    BigDecimal totalRealizedPnl,
    BigDecimal totalPnl
) {}