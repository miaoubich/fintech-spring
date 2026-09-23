package com.miaoubich.ledger.service;

import java.util.List;
import java.util.Optional;

import com.miaoubich.ledger.dto.AccountBalanceResponse;
import com.miaoubich.ledger.dto.LedgerEntryResponse;
import com.miaoubich.ledger.dto.TradeEvent;
import com.miaoubich.ledger.repository.AccountBalanceRepository;
import com.miaoubich.ledger.repository.LedgerEntryRepository;

import jakarta.inject.Singleton;

@Singleton
public class LedgerService {

	private final AccountBalanceRepository accountBalanceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(AccountBalanceRepository accountBalanceRepository,
                            LedgerEntryRepository ledgerEntryRepository) {
        this.accountBalanceRepository = accountBalanceRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }
    
    public Optional<AccountBalanceResponse> getBalance(String userId, String symbol) {
        return accountBalanceRepository.findByUserIdAndSymbol(userId, symbol)
        		.map(balance -> new AccountBalanceResponse(
        				balance.getUserId(),	
        				balance.getSymbol(),
        				balance.getPositionQuantity(),
						balance.getCashBalance(),
						balance.getUpdatedAt()
        				));
        }

	public List<AccountBalanceResponse> findAll() {
		return accountBalanceRepository.findAll()
				.stream().map(balance -> new AccountBalanceResponse(
						balance.getUserId(),	
						balance.getSymbol(),
						balance.getPositionQuantity(),
						balance.getCashBalance(),
						balance.getUpdatedAt()
						)).toList();
	}

	public List<LedgerEntryResponse> findLedgerEntriesByUserId(String userId) {
		return ledgerEntryRepository.findAll()
				.stream()
				.map(entry -> new LedgerEntryResponse(
						entry.getTradeId(),
	                    entry.getUserId(),
						entry.getSymbol(),
						entry.getSide(),
						entry.getAsset(),
						entry.getQuantity(),
						entry.getPrice(),
						entry.getCashAmount(),
						entry.getStatus(),
						entry.getCreatedAt()
				))
				.toList();
	}
}
