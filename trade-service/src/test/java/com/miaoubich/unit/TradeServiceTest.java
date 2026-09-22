package com.miaoubich.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miaoubich.dto.TradeEvent;
import com.miaoubich.dto.TradeResponse;
import com.miaoubich.model.OutboxEvent;
import com.miaoubich.model.Trade;
import com.miaoubich.repository.OutboxEventRepository;
import com.miaoubich.repository.TradeRepository;
import com.miaoubich.service.TradeService;

import io.micronaut.serde.ObjectMapper;

/*
 * Because unit testing the Service layer does not require starting 
 *  the Micronaut HTTP server or opening real Database connection,
 *  we use @ExtendWith(MockitoExtension.class) for ultra-fast isolated
 *  execution
 * */
@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

	private static Logger LOG = LoggerFactory.getLogger(TradeServiceTest.class);
	@Mock
	private OutboxEventRepository outboxEventRepository;
	@Mock
	private TradeRepository tradeRepository;
	@Spy
	private ObjectMapper jsonMapper = ObjectMapper.getDefault();
	@InjectMocks
	private TradeService tradeService;

	private TradeEvent tradeEvent;
	private Trade sampleTrade;

	@BeforeEach
	void setup() {
		tradeEvent = new TradeEvent(
				"tradeId-1",
				"userId-1",
				"BTC-EUR",
				"BUY",
				new BigDecimal("5"),
				new BigDecimal("65000.00"),
				"Crypto",
				"PENDING",
				Instant.now());
		
		sampleTrade = new Trade();
		sampleTrade.setTradeId("tradeId-1");
		sampleTrade.setUserId("userId-1");
		sampleTrade.setSymbol("BTC-EUR");
		sampleTrade.setSide("BUY");
		sampleTrade.setQuantity(new BigDecimal("5"));
		sampleTrade.setPrice(new BigDecimal("65000.00"));
		sampleTrade.setStatus("PENDING");
		sampleTrade.setAsset("Crypto");
		sampleTrade.setCreatedAt(Instant.now());
		//sampleTrade.setUpdatedAt(Instant.now());
	}
	
	/*
	 * 1. pending Trade Test
	 * */
	@Test
	@DisplayName("Should successfully persist trade in PENDING status")
	void createPendingTradeTest() throws IOException {
		LOG.info("tradeEvent -> {}", tradeEvent);
		
		//1. Act
		tradeService.pendingTrade(tradeEvent);
		
		// 2. Assert - Verify trade was saved
		// ArgumentCaptor inspects what was actually passed into tradeRepository.save()
		//  and outboxEventRepository.save()
		ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);
		
		verify(tradeRepository).save(tradeCaptor.capture());
		
		Trade savedTrade = tradeCaptor.getValue();
		
		assertEquals("tradeId-1", savedTrade.getTradeId());
		assertEquals("userId-1", savedTrade.getUserId());
		assertEquals("BTC-EUR", savedTrade.getSymbol());
		assertEquals("BUY", savedTrade.getSide());
		assertEquals(new BigDecimal("5"), savedTrade.getQuantity());
		assertEquals(new BigDecimal("65000.00"), savedTrade.getPrice());
		assertEquals("PENDING", savedTrade.getStatus());
		assertEquals("Crypto", savedTrade.getAsset());
		
		// 3. Assert - Verify ObjectMapper was actually used
		verify(jsonMapper).writeValueAsString(any(TradeEvent.class));
		
		// 4. Assert - Verify outbox event was saved with the payload
		ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
		verify(outboxEventRepository).save(outboxCaptor.capture());

		OutboxEvent savedOutbox = outboxCaptor.getValue();
		assertNotNull(savedOutbox.getPayload()); // Ensure payload (Trade) is not null
		
	}
	
	/*
	 * 2. executeTrade()
	 * */
	@Test
	@DisplayName("Should transition PENDING trade to EXECUTE and create outbox event")
	void executeTradeSuccessfullyTest() {
		String tradeId = "tradeId-1";
		when(tradeRepository.findByTradeId(tradeId)).thenReturn(Optional.of(sampleTrade));
		
		tradeService.executeTrade(tradeId);
		
		// Assert trade status was updated from PENDING to EXECUTED
		ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);
		verify(tradeRepository).update(tradeCaptor.capture());
		LOG.info("tradeCaptor.getValue().getStatus() -> {}", tradeCaptor.getValue().getStatus());
		assertEquals("EXECUTED", tradeCaptor.getValue().getStatus());
		
		// Assert Outbox event was created
		ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
		verify(outboxEventRepository).save(outboxCaptor.capture());
		
		OutboxEvent savedOutbox = outboxCaptor.getValue();
		assertNotNull(savedOutbox);
		assertEquals(tradeId, savedOutbox.getAggregateId());
		assertEquals("TRADE_EXECUTED", savedOutbox.getEventType());
		
	}
	
	/*
	 * Trade already execute exception Test
	 * */
	@Test
	@DisplayName("Should throw Exception if trade is already EXECUTED")
	void shouldThrowExceptionWhenTradeAlreadyExecutedTest() {
		sampleTrade.setStatus("EXECUTED");
		when(tradeRepository.findByTradeId("tradeId-1")).thenReturn(Optional.of(sampleTrade));
		
		assertThrows(IllegalStateException.class, () -> tradeService.executeTrade("tradeId-1"));
		verify(outboxEventRepository, never()).save(any());
	}
	
	/*
	 * Trade not found exception
	 * */
	@Test
	@DisplayName("Should throw Exception when trade does not exist")
	void shouldThrowWhenTradeNotFoundTest() {
		String tradeIdNonExist = "tradId-non-exist";
		when(tradeRepository.findByTradeId(tradeIdNonExist)).thenReturn(Optional.empty());
		
		assertThrows(RuntimeException.class, () -> tradeService.executeTrade(tradeIdNonExist));
		verify(outboxEventRepository, never()).save(any());
	}
	
	/*
	 * Query Methods get all trades
	 * */
	@Test
	@DisplayName("getAllTrades() should return mapped list of trade responses")
	void getAllTradesTest() {
		when(tradeRepository.findAllTradesOrderByCreatedAtDesc()).thenReturn(List.of(sampleTrade));
		
		List<TradeResponse> result = tradeService.getAllTrades();
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("tradeId-1", result.get(0).tradeId());
		
		verify(tradeRepository).findAllTradesOrderByCreatedAtDesc();
	}
	
	/*
	 * Query Method get Trade by userid
	 * */
	@Test
	@DisplayName("getTradeByUserId should return trades filtered by usinrId")
	void returnTradesByUserId() {
		String userId = "userId-1";
		when(tradeRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(sampleTrade));
		
		List<TradeResponse> result = tradeService.getTradesByUserId(userId);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(userId, result.get(0).userId());
		verify(tradeRepository).findByUserIdOrderByCreatedAtDesc(userId);
	}
}
