package com.miaoubich.ledger.controller;

import java.util.List;

import com.miaoubich.ledger.dto.AccountBalanceResponse;
import com.miaoubich.ledger.dto.LedgerEntryResponse;
import com.miaoubich.ledger.service.LedgerService;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @Get("/balance/{userId}/{symbol}")
    public HttpResponse<AccountBalanceResponse> getBalance(String userId, String symbol) {
        return ledgerService.getBalance(userId, symbol)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Get("/entries/{userId}")
    public List<LedgerEntryResponse> getEntries(String userId) {
        return ledgerService.findLedgerEntriesByUserId(userId);
    }

    @Get("/portfolio/{userId}")
    public List<AccountBalanceResponse> getPortfolio(String userId) {
        return ledgerService.findAll()
                .stream()
                .filter(b -> b.userId().equals(userId))
                .toList();
    }

    @Get("/health")
    public String health() {
        return "OK";
    }
}
