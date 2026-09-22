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
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean // Spring Boot 3.4+ standard annotation
    private TradeService tradeService;

    @Test
    @DisplayName("GET /trades/health should return 200 OK")
    void healthCheckTest() throws Exception {
        mockMvc.perform(get("/trades/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Trade service is up and running!"));
    }

    @Test
    @DisplayName("POST /trades should return 200 OK")
    void createTradeTest() throws Exception {
        TradeEvent event = new TradeEvent(
                "tradeId-1", "userId-1", "BTC-EUR", "BUY",
                new BigDecimal("5"), new BigDecimal("65000.00"),
                "Crypto", "PENDING", Instant.now()
        );

        doNothing().when(tradeService).pendingTrade(any(TradeEvent.class));

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /trades/{tradeId}/execute should return 204 No Content")
    void executeTradeTest() throws Exception {
        doNothing().when(tradeService).executeTrade("tradeId-1");

        mockMvc.perform(patch("/trades/tradeId-1/execute"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /trades?userId=... should return filtered list")
    void getTradesByUserIdTest() throws Exception {
        TradeResponse response = new TradeResponse(
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
        

        when(tradeService.getTradesByUserId(eq("userId-1"))).thenReturn(List.of(response));

        mockMvc.perform(get("/trades").param("userId", "userId-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tradeId").value("tradeId-1"));
    }
}