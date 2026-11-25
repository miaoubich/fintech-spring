package com.miaoubich.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoubich.agent.client.UserClient;
import com.miaoubich.agent.client.WalletClient;
import com.miaoubich.agent.dto.AssetBalanceResponse;
import com.miaoubich.agent.dto.PageResponse;
import com.miaoubich.agent.dto.UserResponse;
import com.miaoubich.agent.dto.WalletResponse;
import com.miaoubich.agent.dto.WalletSummaryResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountTools {

    private final WalletClient walletClient;
    private final UserClient userClient;
    private final ObjectMapper objectMapper;

    public AccountTools(WalletClient walletClient, UserClient userClient, ObjectMapper objectMapper) {
        this.walletClient = walletClient;
        this.userClient = userClient;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Retrieve user wallet details including cash balances, portfolio value, total PnL, and open positions.")
    public String getWallet(@ToolParam(description = "The userId of the account holder") 
    					    String userId
    					   ) {
        try {
            Optional<WalletResponse> wallet = walletClient.getWallet(userId);
            if (wallet.isEmpty()) {
                return "No wallet found for userId: " + userId;
            }
            return objectMapper.writeValueAsString(wallet.get());
        } catch (Exception e) {
            return "Error retrieving wallet: " + e.getMessage();
        }
    }

    @Tool(description = "Retrieve balance and locked amount of a specific asset symbol (e.g. BTC, ETH, USD) for a user.")
    public String getAssetBalance(
            @ToolParam(description = "The userId of the account holder") String userId,
            @ToolParam(description = "The asset symbol (e.g. BTC, ETH, USD)") String symbol
    ) {
        try {
            Optional<AssetBalanceResponse> balance = walletClient.getAssetBalance(userId, symbol);
            if (balance.isEmpty()) {
                return "No balance found for asset '" + symbol + "' for userId: " + userId;
            }
            return objectMapper.writeValueAsString(balance.get());
        } catch (Exception e) {
            return "Error retrieving asset balance: " + e.getMessage();
        }
    }

    @Tool(description = "Retrieve total wallet valuation summary in USD and all individual asset balances.")
    public String getWalletSummary(
            @ToolParam(description = "The userId of the account holder") String userId
    ) {
        try {
            Optional<WalletSummaryResponse> summary = walletClient.getWalletSummary(userId);
            if (summary.isEmpty()) {
                return "No wallet summary found for userId: " + userId;
            }
            return objectMapper.writeValueAsString(summary.get());
        } catch (Exception e) {
            return "Error retrieving wallet summary: " + e.getMessage();
        }
    }

    @Tool(description = "Retrieve user directory list (Admin only). Returns paginated user records.")
    public String getAllUsers(
            @ToolParam(description = "Page number (zero-indexed)") int page,
            @ToolParam(description = "Page size") int size
    ) {
        try {
            PageResponse<UserResponse> users = userClient.getAllUsers(page, size);
            if (users == null || users.content().isEmpty()) {
                return "No users found or insufficient permissions.";
            }
            return objectMapper.writeValueAsString(users);
        } catch (Exception e) {
            return "Error retrieving user list: " + e.getMessage();
        }
    }
}