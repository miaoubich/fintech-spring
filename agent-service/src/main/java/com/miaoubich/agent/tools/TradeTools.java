package com.miaoubich.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoubich.agent.client.TradeClient;
import com.miaoubich.agent.dto.TradeResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeTools {

    private final TradeClient tradeClient;
    private final ObjectMapper objectMapper;

    public TradeTools(TradeClient tradeClient, ObjectMapper objectMapper) {
        this.tradeClient = tradeClient;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Retrieve trade order records and statuses (PENDING, EXECUTED, CANCELLED) for a given userId.")
    public String getTradesByUserId(
            @ToolParam(description = "The userId of the trader") String userId
    ) {
        try {
            List<TradeResponse> trades = tradeClient.getTradesByUserId(userId);
            if (trades.isEmpty()) {
                return "No trade records found for userId: " + userId;
            }
            return objectMapper.writeValueAsString(trades);
        } catch (Exception e) {
            return "Error retrieving trades: " + e.getMessage();
        }
    }
}