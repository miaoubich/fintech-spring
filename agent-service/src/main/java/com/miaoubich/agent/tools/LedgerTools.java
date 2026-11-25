package com.miaoubich.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoubich.agent.client.LedgerClient;
import com.miaoubich.agent.dto.AccountBalanceResponse;
import com.miaoubich.agent.dto.LedgerEntryResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LedgerTools {

    private final LedgerClient ledgerClient;
    private final ObjectMapper objectMapper;

    public LedgerTools(LedgerClient ledgerClient, ObjectMapper objectMapper) {
        this.ledgerClient = ledgerClient;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Retrieve immutable audit ledger entries and transaction history for a given userId.")
    public String getLedgerEntries(
            @ToolParam(description = "The userId whose ledger entries to retrieve") String userId
    ) {
        try {
            List<LedgerEntryResponse> entries = ledgerClient.getEntries(userId);
            if (entries.isEmpty()) {
                return "No ledger entries found for userId: " + userId;
            }
            return objectMapper.writeValueAsString(entries);
        } catch (Exception e) {
            return "Error retrieving ledger entries: " + e.getMessage();
        }
    }

    @Tool(description = "Get ledger accounting balance (position quantity and cash balance) for a specific symbol and userId.")
    public String getLedgerBalance(
            @ToolParam(description = "The userId of the account holder") String userId,
            @ToolParam(description = "The asset symbol (e.g. BTC, ETH, USD)") String symbol
    ) {
        try {
            Optional<AccountBalanceResponse> balance = ledgerClient.getBalance(userId, symbol);
            if (balance.isEmpty()) {
                return "No ledger balance found for symbol '" + symbol + "' and userId: " + userId;
            }
            return objectMapper.writeValueAsString(balance.get());
        } catch (Exception e) {
            return "Error retrieving ledger balance: " + e.getMessage();
        }
    }

    @Tool(description = "Get entire ledger portfolio accounting balances for all symbols for a given userId.")
    public String getLedgerPortfolio(
            @ToolParam(description = "The userId of the account holder") String userId
    ) {
        try {
            List<AccountBalanceResponse> portfolio = ledgerClient.getPortfolio(userId);
            if (portfolio.isEmpty()) {
                return "No ledger portfolio records found for userId: " + userId;
            }
            return objectMapper.writeValueAsString(portfolio);
        } catch (Exception e) {
            return "Error retrieving ledger portfolio: " + e.getMessage();
        }
    }
}