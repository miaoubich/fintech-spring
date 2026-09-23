package com.miaoubich.ledger.service;

import com.miaoubich.ledger.dto.TradeEvent;
import com.miaoubich.ledger.model.AccountBalance;
import com.miaoubich.ledger.model.LedgerEntry;
import com.miaoubich.ledger.model.ProcessedTrade;
import com.miaoubich.ledger.repository.AccountBalanceRepository;
import com.miaoubich.ledger.repository.LedgerEntryRepository;
import com.miaoubich.ledger.repository.ProcessedTradeRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@Singleton
public class EventProcessService {

    private static final Logger LOG = LoggerFactory.getLogger(EventProcessService.class);

    private final ProcessedTradeRepository processedTradeRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    
    private static final String EXECUTED = "TRADE_EXECUTED";
    private static final String BUY = "BUY";
    private static final String SELL = "SELL";

    public EventProcessService(
            ProcessedTradeRepository processedTradeRepository,
            LedgerEntryRepository ledgerEntryRepository,
            AccountBalanceRepository accountBalanceRepository
    ) {
        this.processedTradeRepository = processedTradeRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountBalanceRepository = accountBalanceRepository;
    }

    @Transactional
    public void processTradeEvent(TradeEvent event) {

        // Step 1: Validate
        validate(event);

        // Step 2: Only process EXECUTED trades
        if (!EXECUTED.equalsIgnoreCase(event.status())) {
            LOG.info(
                    "Ignoring non-executed trade. tradeId={}, status={}",
                    event.tradeId(),
                    event.status()
            );
            return;
        }

        // Step 3: Idempotency check — skip if already processed
        if (processedTradeRepository.existsByTradeId(event.tradeId())) {
            LOG.warn(
                    "Trade already processed. Skipping. tradeId={}",
                    event.tradeId()
            );
            return;
        }

        // Step 4: Mark as processed FIRST (prevents double-processing)
        processedTradeRepository.save(new ProcessedTrade(event.tradeId()));

        // Step 5: Calculate amounts
        BigDecimal grossAmount = event.quantity().multiply(event.price());

        BigDecimal positionDelta;
        BigDecimal cashDelta;

        if (BUY.equalsIgnoreCase(event.side())) {
            positionDelta = event.quantity();           // gain shares
            cashDelta = grossAmount.negate();            // spend cash
        } else if (SELL.equalsIgnoreCase(event.side())) {
            positionDelta = event.quantity().negate();   // lose shares
            cashDelta = grossAmount;                     // gain cash
        } else {
            throw new IllegalArgumentException("Unknown trade side: " + event.side());
        }

        // Step 6: Build ledger entry
        LedgerEntry ledgerEntry = buildLedgerEntry(event, cashDelta);

        ledgerEntryRepository.save(ledgerEntry);

        // Step 7: Update account balance
        AccountBalance balance = accountBalanceRepository
                .findByUserIdAndSymbol(event.userId(), event.symbol())
                .orElseGet(() -> new AccountBalance(event.userId(), event.symbol()));

        balance.applyDelta(positionDelta, cashDelta);
        accountBalanceRepository.save(balance);

        lodgerUpdatedLog(event, positionDelta, cashDelta);
    }

	private void lodgerUpdatedLog(TradeEvent event, BigDecimal positionDelta, BigDecimal cashDelta) {
		LOG.info(
                "Ledger updated. tradeId={}, userId={}, symbol={}, side={}, " +
                "positionDelta={}, cashDelta={}",
                event.tradeId(),
                event.userId(),
                event.symbol(),
                event.side(),
                positionDelta,
                cashDelta
        );
	}

	private LedgerEntry buildLedgerEntry(TradeEvent event, BigDecimal cashDelta) {
		LedgerEntry ledgerEntry = new LedgerEntry(
                event.tradeId(),
                event.userId(),
                event.symbol(),
                event.side().toUpperCase(),
                event.asset(),
                event.quantity(),
                event.price(),
                cashDelta,
                event.status().toUpperCase()
        );
		return ledgerEntry;
	}

    private void validate(TradeEvent event) {
        if (event.tradeId() == null || event.tradeId().isBlank())
            throw new IllegalArgumentException("tradeId is required");
        if (event.userId() == null || event.userId().isBlank())
            throw new IllegalArgumentException("userId is required");
        if (event.symbol() == null || event.symbol().isBlank())
            throw new IllegalArgumentException("symbol is required");
        if (event.side() == null || event.side().isBlank())
            throw new IllegalArgumentException("side is required");
        if (event.quantity() == null || event.quantity().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("quantity must be positive");
        if (event.price() == null || event.price().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("price must be positive");
        if (event.asset() == null || event.asset().isBlank())
            throw new IllegalArgumentException("asset is required");
        if (event.timestamp() == null)
            throw new IllegalArgumentException("timestamp is required");
    }
}