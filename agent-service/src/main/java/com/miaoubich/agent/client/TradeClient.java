package com.miaoubich.agent.client;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.miaoubich.agent.dto.TradeResponse;

@Service
public class TradeClient {

	private static final Logger LOG = LoggerFactory.getLogger(TradeClient.class);
    private final RestClient restClient;

    public TradeClient(RestClient.Builder builder) {
        this.restClient = builder
        						.baseUrl("http://trade-service:8081")
        						.build();
    }

    public List<TradeResponse> getTradesByUserId(String userId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/trades")
                            .queryParam("userId", userId)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TradeResponse>>() {});
        } catch (Exception e) {
            LOG.error("Failed to fetch trades for userId '{}': {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<TradeResponse> getTradesByUsername(String username) {
        try {
            return restClient.get()
                    .uri("/trades/user/{username}", username)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TradeResponse>>() {});
        } catch (Exception e) {
            LOG.error("Failed to fetch trades for user '{}': {}", username, e.getMessage());
            return Collections.emptyList();
        }
    }

	public TradeResponse getTradeById(Long tradeId) {
		// TODO Auto-generated method stub
		return null;
	}
}