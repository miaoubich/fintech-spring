package com.miaoubich.agent.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.miaoubich.agent.dto.TradeDTO;

@Service
public class TradeClient {

    private final RestClient restClient;

    public TradeClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://trade-service:8081").build();
    }

    public List<TradeDTO> listTrades(String token, String username) {
        return restClient.get()
            .uri("/trade/trades?username={username}", username)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }

    public TradeDTO getTradeById(String token, String tradeId) {
        return restClient.get()
            .uri("/trade/trades/{tradeId}", tradeId)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(TradeDTO.class);
    }
}