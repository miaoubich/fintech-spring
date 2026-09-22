package com.miaoubich.controller;

import java.util.List;
import java.util.Optional;

import com.miaoubich.dto.TradeEvent;
import com.miaoubich.dto.TradeResponse;
import com.miaoubich.service.TradeService;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;

@Controller("/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @Post
    public void createTrade(@Body TradeEvent event) {
        tradeService.pendingTrade(event);
    }
    
    @Get
    public List<TradeResponse> getTrades(@QueryValue Optional<String> userId) {
    	return userId
                .map(tradeService::getTradesByUserId)
                .orElseGet(tradeService::getAllTrades);
	}
    
    @Patch("/{tradeId}/execute")
    public HttpResponse<Void> executeTrade(String tradeId) {
        tradeService.executeTrade(tradeId);
        return HttpResponse.noContent();
    }
    
    @Get("/health")
    public String healthCheck() {
		return "Trade Service is up and running!";
	}
    
}