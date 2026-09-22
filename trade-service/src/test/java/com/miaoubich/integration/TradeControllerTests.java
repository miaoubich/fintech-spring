package com.miaoubich.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.miaoubich.dto.TradeEvent;
import com.miaoubich.dto.TradeResponse;
import com.miaoubich.service.TradeService;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class TradeControllerTests {

	// Micronaut's non blocking HTTP client injected directly into the test
	@Inject
	@Client("/")
	HttpClient client;
	
	@Inject
	TradeService tradeService;
	
	/*
	 * Mocronaut's dependency injection replaces the real TradeService bean
	 *  with the Mockito mock instance for this test suite
	*/
	@MockBean(TradeService.class)
	TradeService tradeService() {
		return Mockito.mock(TradeService.class);
	}
	
	@BeforeEach
	void setup() {
		Mockito.reset(tradeService);
	}
	
	/*
	 * 1. Health Check Test
	 * */
	@Test
	@DisplayName("GET /trades/health should return 200 OK with running message")
	void healthCheckTest() {
		// we use toBlocking() method to execute the HTTP request synchronously 
		HttpResponse<String> response = client.toBlocking().exchange(
					HttpRequest.GET("/trades/health"), String.class
				);
		
		assertEquals(HttpStatus.OK, response.getStatus());
		assertEquals("Trade Service is up and running!", response.getBody().orElse(null));
	}
	
	/*
	 * 2. Create Trade (POST /trades)
	 * */
	@Test
	@DisplayName("POST /trades should invoke tradeService.pendingTrade and return 200 OK")
	void createTradeTest() {
		TradeEvent tradeEvent = createSampeTradeEvent();
		doNothing().when(tradeService).pendingTrade(any(TradeEvent.class));
		
		HttpResponse<Void> response = client.toBlocking().exchange(
					HttpRequest.POST("/trades", tradeEvent), Void.class
				);
		
		assertEquals(HttpStatus.OK, response.getStatus());
		verify(tradeService).pendingTrade(any(TradeEvent.class));
	}

	/*
	 * 3. GET All Trades (GET /trades without userId)
	 * */
	@Test
	@DisplayName("GET /trades without userId should return all trades")
	void getAllTradesTest() {
		TradeResponse mockedResponse = createSampleTradeResponse("trade-1", "user-1");
		when(tradeService.getAllTrades()).thenReturn(List.of(mockedResponse));
		
		HttpResponse<List<TradeResponse>> response = client.toBlocking().exchange(
					HttpRequest.GET("/trades"), Argument.listOf(TradeResponse.class)
				); 
		
		assertEquals(HttpStatus.OK, response.getStatus());
		assertNotNull(response.body());
		assertEquals(1, response.body().size());
		verify(tradeService).getAllTrades();
	}
	
	/*
	 * 4. Get Trades by userId (GET /trades?userId=...)
	 * */
	@Test
	@DisplayName("GET /trades?userId?=... should return filtered trades for user")
	void getTradesByUserIdTest() {
		String userId = "userId-sample";
		TradeResponse mockedResponse = createSampleTradeResponse("trade-2", userId);
		when(tradeService.getTradesByUserId(eq(userId))).thenReturn(List.of(mockedResponse));
	
		HttpResponse<List<TradeResponse>> response = client.toBlocking().exchange(
					HttpRequest.GET("trades?userId=" + userId),
					Argument.listOf(TradeResponse.class)
				);
		
		assertEquals(HttpStatus.OK, response.getStatus());
		assertNotNull(response.body());
		assertEquals(1, response.body().size());
		verify(tradeService).getTradesByUserId(eq(userId));
	} 
	/*
	 * 5. Execute Trade by changing its status to "execute" (PATCH /trades/{tradeId}/execute)
	 * */
	@Test
	@DisplayName("PATCH /trades/{tradeId}/execute should invoke tradeService.executeTrade and returns 204 No Content")
	void executeTradeTest() {
		String tradeId = "tradeId-sample";
		doNothing().when(tradeService).executeTrade(eq(tradeId));
		
		HttpResponse<Void> response = client.toBlocking().exchange(
					HttpRequest.PATCH("/trades/" + tradeId + "/execute", ""), Void.class
				);
		
		assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
		verify(tradeService).executeTrade(eq(tradeId));
			
	}
	
	private TradeEvent createSampeTradeEvent() {
		return new TradeEvent(
				"tradeId-sample",
				"userId-sample",
				"BTC-EUR",
				"BUY",
				new BigDecimal("5"),
				new BigDecimal("65000.00"),
				"Crypto",
				"PENDING",
				Instant.now()
				);
	}
	
	private TradeResponse createSampleTradeResponse(String tradeId, String userId) {
		return new TradeResponse(
				tradeId,
				userId,
				"BTC-EUR",
				"BUY",
				new BigDecimal("5"),
				new BigDecimal("65000.00"),
				"Crypto",
				"PENDING",
				Instant.now(),
				Instant.now()
				);
	}

}
