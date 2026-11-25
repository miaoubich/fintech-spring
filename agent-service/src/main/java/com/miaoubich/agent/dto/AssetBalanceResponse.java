package com.miaoubich.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssetBalanceResponse(
        String userId,
        String symbol,
        BigDecimal balance,
        BigDecimal locked
) {}