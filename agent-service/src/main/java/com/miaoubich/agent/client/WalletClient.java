package com.miaoubich.agent.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.miaoubich.agent.dto.WalletBalance;

@Service
public class WalletClient {

	private final RestClient restClient;
	
	public WalletClient(RestClient.Builder builder) {
		this.restClient = (RestClient) builder.baseUrl("http://wallet-service:8083");
	}
	
	public WalletBalance gatBalance(String token, String username) {
		return restClient.get()
				.uri("/wallet/balance/{username}", username)
				.header("Authorization", "Bearer " + token)
				.retrieve()
				.body(WalletBalance.class);
	}
}
