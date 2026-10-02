package com.miaoubich.wallet.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.math.BigDecimal;

@Serdeable
public record AssetBalanceResponse(
		
    String userId,
    String symbol,
    BigDecimal quantity,
    BigDecimal avgCost,
    BigDecimal currentPrice,
    BigDecimal marketValue,
    BigDecimal unrealizedPnl
) {}