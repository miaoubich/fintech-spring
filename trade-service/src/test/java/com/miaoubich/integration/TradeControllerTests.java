package com.miaoubich.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoubich.controller.TradeController;
import com.miaoubich.dto.TradeEvent;
import com.miaoubich.dto.TradeResponse;
import com.miaoubich.service.TradeService;

@WebMvcTest(TradeController.class)
class TradeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Spring Boot 3.4+ standard annotation
    @MockitoBean 
    private TradeService tradeService;

    @Test
    @DisplayName("GET /trades/health should return 200 OK")
    void healthCheckTest() throws Exception {
        mockMvc.perform(get("/trades/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Trade Service is up and running!"));
    }

    @Test
    @DisplayName("POST /trades should return 200 OK")
    void createTradeTest() throws Exception {
        TradeEvent event = new TradeEvent(
                "tradeId-1", "userId-1", "BTC-EUR", "BUY",
                new BigDecimal("5"), new BigDecimal("65000.00"),
                "Crypto", "PENDING", Instant.now()
        );
        String eventString = objectMapper.writeValueAsString(event);
        
        doNothing().when(tradeService).pendingTrade(any(TradeEvent.class));

		mockMvc.perform(post("/trades").contentType(MediaType.APPLICATION_JSON).content(eventString))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /trades/{tradeId}/execute should return 204 No Content")
    void executeTradeTest() throws Exception {
    	String tradeId = "tradeId-1";
    	
        doNothing().when(tradeService).executeTrade("tradeId-1");

        mockMvc.perform(patch("/trades/" + tradeId + "/execute"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /trades?userId=... should return filtered list")
    void getTradesByUserIdTest() throws Exception {
    	String userId = "userId-1";
    	String tradeId = "tradeId-1";
    	
        TradeResponse response1 = new TradeResponse(
                "tradeId-1", 
                "userId-1", 
                "BTC-EUR", 
                "BUY",
                new BigDecimal("5"), 
                new BigDecimal("65000.00"),
                "Crypto", 
                "PENDING", 
                Instant.now(), 
                null
              );
        TradeResponse response2 = new TradeResponse(
                "tradeId-2", 
                "userId-2", 
                "USD-EUR", 
                "SELL",
                new BigDecimal("5"), 
                new BigDecimal("1.15"),
                "Exchange", 
                "PENDING", 
                Instant.now(), 
                null
              );
        

        when(tradeService.getTradesByUserId(eq(userId))).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/trades").param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tradeId").value(tradeId));
    }
}