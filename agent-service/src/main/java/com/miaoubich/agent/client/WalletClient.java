package com.miaoubich.agent.client;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.miaoubich.agent.dto.AssetBalanceResponse;
import com.miaoubich.agent.dto.WalletResponse;
import com.miaoubich.agent.dto.WalletSummaryResponse;

@Service
public class WalletClient {

	private static final Logger LOG = LoggerFactory.getLogger(WalletClient.class);
	private final RestClient restClient;
	
	public WalletClient(RestClient.Builder restClient) {
		this.restClient = restClient
				.baseUrl("http://wallet-service:8083")
				.build();
	}
	
	public Optional<WalletResponse> getWallet(String userId) {
        try {
            WalletResponse response = restClient.get()
                    .uri("/wallet/{userId}", userId)
                    .retrieve()
                    .body(WalletResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            LOG.error("Failed to fetch wallet for userId '{}': {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<AssetBalanceResponse> getAssetBalance(String userId, String symbol) {
        try {
            AssetBalanceResponse response = restClient.get()
                    .uri("/wallet/{userId}/{symbol}", userId, symbol)
                    .retrieve()
                    .body(AssetBalanceResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            LOG.error("Failed to fetch asset balance for userId '{}', symbol '{}': {}", userId, symbol, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<WalletSummaryResponse> getWalletSummary(String userId) {
        try {
            WalletSummaryResponse response = restClient.get()
                    .uri("/wallet/{userId}/summary", userId)
                    .retrieve()
                    .body(WalletSummaryResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            LOG.error("Failed to fetch wallet summary for userId '{}': {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

}
