package com.miaoubich.wallet.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miaoubich.wallet.dto.AssetBalanceResponse;
import com.miaoubich.wallet.dto.WalletResponse;
import com.miaoubich.wallet.dto.WalletSummaryResponse;
import com.miaoubich.wallet.entity.CashBalance;
import com.miaoubich.wallet.entity.Position;
import com.miaoubich.wallet.repository.CashBalanceRepository;
import com.miaoubich.wallet.repository.PositionRepository;

@Service
public class WalletService {
	
	private final Logger LOG = LoggerFactory.getLogger(WalletService.class);
	
	private final CashBalanceRepository cashBalanceRepository;
	private final PositionRepository positionRepository;

	public WalletService(CashBalanceRepository cashBalanceRepository, PositionRepository positionRepository) {
		this.cashBalanceRepository = cashBalanceRepository;
		this.positionRepository = positionRepository;
	}
	
	@Transactional(readOnly = true)
	public WalletResponse getWallet(String userId) {
		// Get cash balance
		List<CashBalance> cashBalances = cashBalanceRepository.findByUserId(userId);
		
		// Get positions
		List<Position> positions = positionRepository.findByUserId(userId);
		
		//Calculate total
		BigDecimal totlaCashBalance = cashBalances.stream()
				.map(CashBalance::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		BigDecimal totalPortfolioValue = positions.stream()
				.map(Position::getMarketValue)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		BigDecimal totalPnl = positions.stream()
				.map(Position::getUnrealizedPnl)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		// Map to DTOs
		List<WalletResponse.CashBalance> cashBalanceResponses = cashBalances.stream()
				.map(cb -> new WalletResponse.CashBalance(cb.getCurrency(), cb.getAmount()))
				.toList();
		
		List<WalletResponse.Position> positionResponses = positions.stream()
				.map(p -> new WalletResponse.Position(
					 p.getSymbol(),
					 p.getAssetClass(),
					 p.getQuantity(),
					 p.getAvgCost(),
					 p.getCurrentPrice(),
					 p.getMarketValue(),
					 p.getUnrealizedPnl()
					 ))
				.toList();
		
		return new WalletResponse(
				userId,
				totlaCashBalance,
				totalPortfolioValue,
				totalPnl,
				cashBalanceResponses, 
				positionResponses
				);
	}
	
	@Transactional(readOnly = true)
	public AssetBalanceResponse getAssetBalance(String userId, String symbol) {
		Position position = positionRepository.findByUserIdAndSymbol(userId, symbol)
								.orElseThrow(() -> new RuntimeException("Position not found!"));
		
		return new AssetBalanceResponse(
					userId, 
					symbol, 
					position.getQuantity(), 
					position.getAvgCost(), 
					position.getCurrentPrice(), 
					position.getMarketValue(), 
					position.getUnrealizedPnl()
				);
	}

	@Transactional(readOnly = true)
	public WalletSummaryResponse getWalletSummary(String userId) {
		// Get cash balance
		List<CashBalance> cashBalances = cashBalanceRepository.findByUserId(userId);
		BigDecimal totalCashValue = cashBalances.stream()
						.map(CashBalance::getAmount)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		// Get positions
		List<Position> positions = positionRepository.findByUserId(userId);
		BigDecimal totalPortfolioValue = positions.stream()
						.map(Position::getMarketValue)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		BigDecimal totalUnrealizedPnl = positions.stream()
						.map(Position::getUnrealizedPnl)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		// Let's just take totalRealizedPnlas zero
		BigDecimal totalRealizedPnl = BigDecimal.ZERO;
		
		BigDecimal totalPnl = totalUnrealizedPnl.add(totalRealizedPnl);
		
		return new WalletSummaryResponse(
					userId,
					totalPortfolioValue,
					totalCashValue,
					totalUnrealizedPnl,
					totalRealizedPnl,
					totalPnl
				);
	}
}