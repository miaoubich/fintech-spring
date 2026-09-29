package com.miaoubich.ledger.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miaoubich.ledger.dto.AccountBalanceResponse;
import com.miaoubich.ledger.dto.LedgerEntryResponse;
import com.miaoubich.ledger.service.LedgerService;

@RestController
@RequestMapping("/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/balance/{userId}/{symbol}")
    public ResponseEntity<AccountBalanceResponse> getBalance(
    									@PathVariable String userId, 
    									@PathVariable String symbol) {
        return ledgerService.getBalance(userId, symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/entries/{userId}")
    public ResponseEntity<List<LedgerEntryResponse>> getEntries(String userId) {
    	List<LedgerEntryResponse> response = ledgerService
    											.findLedgerEntriesByUserId(userId);
    	return ResponseEntity.ok(response);
    }

    @GetMapping("/portfolio/{userId}")
    public ResponseEntity<List<AccountBalanceResponse>> getPortfolio(String userId) {
    	List<AccountBalanceResponse> response = ledgerService.findAll()
										                .stream()
										                .filter(b -> b.userId().equals(userId))
										                .toList();
    	return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
