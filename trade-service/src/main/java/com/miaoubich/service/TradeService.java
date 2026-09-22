package com.miaoubich.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoubich.dto.TradeEvent;
import com.miaoubich.dto.TradeResponse;
import com.miaoubich.model.OutboxEvent;
import com.miaoubich.model.Trade;
import com.miaoubich.repository.OutboxEventRepository;
import com.miaoubich.repository.TradeRepository;

import jakarta.transaction.Transactional;

@Service
public class TradeService {

	private static final Logger LOG = LoggerFactory.getLogger(TradeService.class);
	
    private final OutboxEventRepository outboxEventRepository;
    private final TradeRepository tradeRepository;
    private final ObjectMapper jsonMapper;

    public TradeService(OutboxEventRepository outboxEventRepository, ObjectMapper jsonMapper,
    		TradeRepository tradeRepository) {
		this.outboxEventRepository = outboxEventRepository;
		this.tradeRepository = tradeRepository;
		this.jsonMapper = jsonMapper;
    }

    @Transactional
    public void pendingTrade(TradeEvent tradeEvent) {

        Instant now = Instant.now();
        // status in payload
        TradeEvent tradeEventPending = tradeEvent.withStatus(TradeEvent.STATUS_PENDING, now);

        Trade trade = new Trade(
                tradeEventPending.tradeId(),
                tradeEventPending.userId(),
                tradeEventPending.symbol(),
                tradeEventPending.side(),
                tradeEventPending.quantity(),
                tradeEventPending.price(),
                tradeEventPending.status(),
                tradeEventPending.asset(),
                now
        );

        String payload = serializePayload(tradeEventPending);

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(TradeEvent.EVENT_TYPE_CREATED);  // event type on outbox
        outboxEvent.setAggregateId(tradeEventPending.tradeId());
        outboxEvent.setAggregateType(TradeEvent.AGGREGATE_TYPE);
        outboxEvent.setPayload(payload);
        outboxEvent.setCreatedAt(now);
        outboxEvent.setProcessed(false);

        tradeRepository.save(trade);
        outboxEventRepository.save(outboxEvent);

        LOG.info("Trade created. tradeId={}", trade.getTradeId());
    }

    public List<TradeResponse> getAllTrades() {
        return tradeRepository.findAllTradesOrderByCreatedAtDesc()
                .stream()
                .map(TradeResponse::from)
                .toList();
    }
    
    public List<TradeResponse> getTradesByUserId(String userId) {
        return tradeRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TradeResponse::from)
                .toList();
    }
    
	private @NonNull String serializePayload(TradeEvent tradeEventPending) {
		try {
			return new String(jsonMapper.writeValueAsString(tradeEventPending));
		} catch (IOException e) {
			LOG.error("Failed to serialize TradeEvent payload", e);
			throw new RuntimeException("Failed to serialize TradeEvent payload", e);
		}
	}

	@Transactional
	public void executeTrade(String tradeId) {
	    Trade trade = tradeRepository.findByTradeId(tradeId)
	            .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeId));

	    if (!TradeEvent.STATUS_PENDING.equalsIgnoreCase(trade.getStatus())) {
	        throw new IllegalStateException(
	                "Only PENDING trades can be executed. Current status: " + trade.getStatus()
	        );
	    }

	    trade.setStatus(TradeEvent.STATUS_EXECUTED);
	    trade.setUpdatedAt(Instant.now());
	    tradeRepository.save(trade);

	    TradeEvent executedEvent = new TradeEvent(
	            trade.getTradeId(),
	            trade.getUserId(),
	            trade.getSymbol(),
	            trade.getSide(),
	            trade.getQuantity(),
	            trade.getPrice(),
	            trade.getAsset(), // asset, if Trade entity does not have it
	            trade.getStatus(),
	            trade.getUpdatedAt()
	    );
	    executedEvent.withStatus(TradeEvent.STATUS_EXECUTED, Instant.now());

	    String payload = serializePayload(executedEvent);

	    OutboxEvent outboxEvent = new OutboxEvent();
	    outboxEvent.setEventType(TradeEvent.EVENT_TYPE_EXECUTED);
	    outboxEvent.setAggregateId(trade.getTradeId());
	    outboxEvent.setAggregateType(TradeEvent.AGGREGATE_TYPE);
	    outboxEvent.setPayload(payload);
	    outboxEvent.setCreatedAt(Instant.now());
	    outboxEvent.setProcessed(false);

	    outboxEventRepository.save(outboxEvent);

	    LOG.info("Trade executed and outbox event created. tradeId={}", tradeId);
	}
}
