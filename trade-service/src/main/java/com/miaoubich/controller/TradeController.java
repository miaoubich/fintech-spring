package com.miaoubich.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miaoubich.dto.TradeEvent;
import com.miaoubich.dto.TradeResponse;
import com.miaoubich.service.TradeService;

@RestController
@RequestMapping("/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<Void> createTrade(@RequestBody TradeEvent event) {
        tradeService.pendingTrade(event);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    public List<TradeResponse> getTrades(@RequestParam Optional<String> userId) {
    	return userId
                .map(tradeService::getTradesByUserId)
                .orElseGet(tradeService::getAllTrades);
	}
    
    @PatchMapping("/{tradeId}/execute")
    public ResponseEntity<Void> executeTrade(String tradeId) {
        tradeService.executeTrade(tradeId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/health")
    public String healthCheck() {
		return "Trade Service is up and running!";
	}
    
}