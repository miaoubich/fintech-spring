package com.miaoubich.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountBalanceResponse(
        String userId,
        String symbol,
        BigDecimal positionQuantity,
        BigDecimal cashBalance,
        Instant updatedAt
) {}