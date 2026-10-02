package com.miaoubich.wallet.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.math.BigDecimal;

@Serdeable
public record WalletSummaryResponse(
		
    String userId,
    BigDecimal totalPortfolioValue,
    BigDecimal totalCashValue,
    BigDecimal totalUnrealizedPnl, //Profit or Loss on positions you still hold
    BigDecimal totalRealizedPnl,
    BigDecimal totalPnl
) {}