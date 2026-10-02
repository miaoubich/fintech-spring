package com.miaoubich.wallet.dto;

import java.math.BigDecimal;

public record AssetBalanceResponse(
		
    String userId,
    String symbol,
    BigDecimal quantity,
    BigDecimal avgCost,
    BigDecimal currentPrice,
    BigDecimal marketValue,
    BigDecimal unrealizedPnl
) {}