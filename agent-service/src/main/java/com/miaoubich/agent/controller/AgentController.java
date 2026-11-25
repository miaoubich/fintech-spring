package com.miaoubich.agent.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miaoubich.agent.dto.ChatResponse;
import com.miaoubich.agent.tools.AccountTools;
import com.miaoubich.agent.tools.LedgerTools;
import com.miaoubich.agent.tools.TradeTools;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final ChatClient chatClient;

    public AgentController(
            ChatModel chatModel,
            AccountTools accountTools,
            TradeTools tradeTools,
            LedgerTools ledgerTools
    ) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                    You are a strictly READ-ONLY Fintech Account, Portfolio, and Trading Assistant.
                    You can look up wallet balances, cash balances, positions, PnL, trade history, and ledger journals.
                    You MUST NEVER execute trades, modify balances, create transfer requests, or alter any state.
                    When querying tools, use the authenticated user's userId or username from context if not explicitly specified.
                    Always present currency amounts, position values, and timestamps in a clear and organized format.
                """)
                .defaultTools(accountTools, tradeTools, ledgerTools)
                .build();
    }

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(
            @RequestBody String message,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getClaimAsString("preferred_username");
        String userId = jwt.getSubject();

        String content = chatClient.prompt()
                .system(systemPrompt -> systemPrompt.text("Authenticated user context: username={username}, userId={userId}")
                        .param("username", username != null ? username : "")
                        .param("userId", userId != null ? userId : username))
                .user(message)
                .call()
                .content();

        return ChatResponse.of(content);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(
            @RequestBody String message,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getClaimAsString("preferred_username");
        String userId = jwt.getSubject();

        return chatClient.prompt()
                .system(systemPrompt -> systemPrompt.text("Authenticated user context: username={username}, userId={userId}")
                        .param("username", username != null ? username : "")
                        .param("userId", userId != null ? userId : username))
                .user(message)
                .stream()
                .content();
    }
}