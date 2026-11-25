package com.miaoubich.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WalletSummaryResponse(
        String userId,
        BigDecimal totalValueUsd,
        List<AssetBalanceResponse> balances
) {}