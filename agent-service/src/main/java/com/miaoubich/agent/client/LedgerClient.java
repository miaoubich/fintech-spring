package com.miaoubich.agent.client;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.miaoubich.agent.dto.AccountBalanceResponse;
import com.miaoubich.agent.dto.LedgerEntryResponse;

@Component
public class LedgerClient {

    private static final Logger LOG = LoggerFactory.getLogger(LedgerClient.class);
    private final RestClient restClient;

    public LedgerClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://LEDGER-SERVICE:8082")
                .build();
    }

    public Optional<AccountBalanceResponse> getBalance(String userId, String symbol) {
        try {
            AccountBalanceResponse response = restClient.get()
                    .uri("/ledger/balance/{userId}/{symbol}", userId, symbol)
                    .retrieve()
                    .body(AccountBalanceResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            LOG.error("Failed to fetch ledger balance for userId '{}', symbol '{}': {}", userId, symbol, e.getMessage());
            return Optional.empty();
        }
    }

    public List<LedgerEntryResponse> getEntries(String userId) {
        try {
            return restClient.get()
                    .uri("/ledger/entries/{userId}", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LedgerEntryResponse>>() {});
        } catch (Exception e) {
            LOG.error("Failed to fetch ledger entries for userId '{}': {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<AccountBalanceResponse> getPortfolio(String userId) {
        try {
            return restClient.get()
                    .uri("/ledger/portfolio/{userId}", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AccountBalanceResponse>>() {});
        } catch (Exception e) {
            LOG.error("Failed to fetch portfolio for userId '{}': {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }
}
