package com.miaoubich.wallet.service;

import com.miaoubich.wallet.dto.TradeEvent;
import com.miaoubich.wallet.entity.CashBalance;
import com.miaoubich.wallet.entity.Position;
import com.miaoubich.wallet.repository.CashBalanceRepository;
import com.miaoubich.wallet.repository.PositionRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Singleton
public class WalletProjectionService {

    private static final Logger LOG = LoggerFactory.getLogger(WalletProjectionService.class);
    private static final BigDecimal INITIAL_DEMO_BALANCE = new BigDecimal("100000.00");
    private static final String EXECUTED = "TRADE_EXECUTED";
    private static final String BUY = "BUY";
    private static final String SELL = "SELL";

    private final CashBalanceRepository cashBalanceRepository;
    private final PositionRepository positionRepository;

    public WalletProjectionService(CashBalanceRepository cashBalanceRepository,
                                   PositionRepository positionRepository) {
        this.cashBalanceRepository = cashBalanceRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional
    public void processTradeEvent(TradeEvent event) {
        // We only project EXECUTED trades to the wallet view
        if (!EXECUTED.equalsIgnoreCase(event.status())) {
            LOG.debug("Skipping non-executed trade: tradeId={}, status={}", event.tradeId(), event.status());
            return;
        }

        String userId = event.userId();
        String symbol = event.symbol();
        String side = event.side();
        BigDecimal quantity = event.quantity();
        BigDecimal price = event.price();
        String assetClass = event.asset();

        // Extract currency & asset symbol from pair (e.g. "BTC-EUR" -> asset: "BTC", currency: "EUR")
        String currency = symbol.contains("-") ? symbol.split("-")[1] : "EUR";
        String assetSymbol = symbol.contains("-") ? symbol.split("-")[0] : symbol;

        BigDecimal tradeCost = quantity.multiply(price);

        // 1. Update Cash Balance
        updateCashBalance(userId, currency, side, tradeCost);

        // 2. Update Asset Positions
        updateAssetPosition(userId, assetSymbol, assetClass, side, quantity, price);
    }

    private void updateCashBalance(String userId, String currency, String side, BigDecimal tradeCost) {
        CashBalance cashBalance = cashBalanceRepository.findByUserIdAndCurrency(userId, currency)
                .orElseGet(() -> {
                    LOG.info("Initializing new cash balance for user: {} with {} {}", userId, INITIAL_DEMO_BALANCE, currency);
                    return new CashBalance(userId, currency, INITIAL_DEMO_BALANCE);
                });

        BigDecimal currentAmount = cashBalance.getAmount();
        BigDecimal newAmount = BUY.equalsIgnoreCase(side)? currentAmount.subtract(tradeCost): currentAmount.add(tradeCost);

        cashBalance.setAmount(newAmount);

        if (cashBalance.getId() == null) {
            cashBalanceRepository.save(cashBalance);
        } else {
            cashBalanceRepository.update(cashBalance);
        }

        LOG.info("Updated cash balance for user {}: {} {}", userId, newAmount, currency);
    }

    private void updateAssetPosition(String userId, String symbol, String assetClass,
                                    String side, BigDecimal quantity, BigDecimal price) {
        Position position = positionRepository.findByUserIdAndSymbol(userId, symbol)
                .orElse(null);

        if (BUY.equalsIgnoreCase(side)) {
            handleBuyPosition(userId, symbol, assetClass, quantity, price, position);
        } else if (SELL.equalsIgnoreCase(side)) {
            handleSellPosition(userId, symbol, quantity, price, position);
        }
    }

    private void handleBuyPosition(String userId, String symbol, String assetClass,
                                   BigDecimal quantity, BigDecimal price, Position position) {
        if (position == null) {
            Position newPosition = new Position(userId, symbol, assetClass, quantity, price, price);
            positionRepository.save(newPosition);
            LOG.info("Opened new position for user {}: {} {}", userId, quantity, symbol);
        } else {
            BigDecimal currentQty = position.getQuantity();
            BigDecimal currentAvgCost = position.getAvgCost();

            BigDecimal newQty = currentQty.add(quantity);
            BigDecimal totalCost = (currentQty.multiply(currentAvgCost)).add(quantity.multiply(price));
            BigDecimal newAvgCost = totalCost.divide(newQty, 8, RoundingMode.HALF_UP);

            position.setQuantity(newQty);
            position.setAvgCost(newAvgCost);
            position.setCurrentPrice(price);
            positionRepository.update(position);

            LOG.info("Updated position for user {}: newQty={}, newAvgCost={}", userId, newQty, newAvgCost);
        }
    }

    private void handleSellPosition(String userId, String symbol, BigDecimal quantity,
                                    BigDecimal price, Position position) {
        if (position == null) {
            LOG.warn("Attempted to sell asset with no open position. userId={}, symbol={}", userId, symbol);
            return;
        }

        BigDecimal currentQty = position.getQuantity();
        BigDecimal remainingQty = currentQty.subtract(quantity);

        if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
            positionRepository.delete(position);
            LOG.info("Fully closed position for user {}: {}", userId, symbol);
        } else {
            position.setQuantity(remainingQty);
            position.setCurrentPrice(price);
            positionRepository.update(position);
            LOG.info("Reduced position for user {}: remainingQty={}", userId, remainingQty);
        }
    }
}