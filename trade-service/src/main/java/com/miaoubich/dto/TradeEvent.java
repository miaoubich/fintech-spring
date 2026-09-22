package com.miaoubich.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeEvent(

		String tradeId, 
		String userId, 
		String symbol, // BTC-EUR, ETH-EUR, EUR-USD, USD-JPY
		String side, // BYU or SELL
		BigDecimal quantity, 
		BigDecimal price, 
		String asset, // AAPL, TSLA, MSFT, BTC, ETH, EUR, USD
		String status, // PENDING, EXECUTED, CANCELLED
		Instant timestamp) 
{
	public static final String AGGREGATE_TYPE = "Trade";
	 // What state the trade is in (goes in the payload)
    public static final String STATUS_PENDING  = "PENDING";
    public static final String STATUS_EXECUTED = "EXECUTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // What happened (goes on the outbox row / Kafka header)
    public static final String EVENT_TYPE_CREATED  = "TRADE_CREATED";
    public static final String EVENT_TYPE_EXECUTED = "TRADE_EXECUTED";
    public static final String EVENT_TYPE_CANCELLED = "TRADE_CANCELLED";

	public TradeEvent withStatus(String newStatus, Instant newTimestamp) {
    	return new TradeEvent(
    			tradeId, 
    			userId, 
    			symbol, 
    			side, 
    			quantity, 
    			price, 
    			asset,
    			newStatus, 
    			newTimestamp
    		);
    }
}
