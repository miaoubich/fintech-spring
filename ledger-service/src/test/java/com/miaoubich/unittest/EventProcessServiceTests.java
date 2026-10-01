package com.miaoubich.unittest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.miaoubich.ledger.dto.TradeEvent;
import com.miaoubich.ledger.model.AccountBalance;
import com.miaoubich.ledger.model.LedgerEntry;
import com.miaoubich.ledger.model.ProcessedTrade;
import com.miaoubich.ledger.repository.AccountBalanceRepository;
import com.miaoubich.ledger.repository.LedgerEntryRepository;
import com.miaoubich.ledger.repository.ProcessedTradeRepository;
import com.miaoubich.ledger.service.EventProcessService;

@ExtendWith(MockitoExtension.class)
public class EventProcessServiceTests {

    @Mock
    private ProcessedTradeRepository processedTradeRepository;
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private AccountBalanceRepository accountBalanceRepository;

    @InjectMocks
    private EventProcessService eventProcessService;

    private TradeEvent executedBuyTradeEvent;
    private TradeEvent pendingTradeEvent;
    private TradeEvent invalidTradeEvent;

    @BeforeEach
    void setUp() {
        executedBuyTradeEvent = new TradeEvent(
                "tradeId-1",
                "userId-1",
                "BTC-EUR",
                "BUY",
                "Crypto",
                new BigDecimal("2"),
                new BigDecimal("500.00"),
                "TRADE_EXECUTED",
                Instant.now()
        );
        pendingTradeEvent = new TradeEvent(
                "tradeId-1",
                "userId-1",
                "BTC-EUR",
                "BUY",
                "Crypto",
                new BigDecimal("2"),
                new BigDecimal("50000.00"),
                "PENDING",
                Instant.now()
        );
        invalidTradeEvent = new TradeEvent(
                "",
                "userId-1",
                "BTC-EUR",
                "BUY",
                "Crypto",
                new BigDecimal("2"),
                new BigDecimal("50000.00"),
                "PENDING",
                Instant.now()
        );
    }

    @Test
    @DisplayName("Should process EXECUTED BUY trade: deduct cash, add position")
    void shouldProcessExecutedBuyTrade() {
        when(processedTradeRepository.existsByTradeId("tradeId-1")).thenReturn(false);
        when(accountBalanceRepository.findByUserIdAndSymbol("userId-1", "BTC-EUR"))
                .thenReturn(Optional.of(new AccountBalance("userId-1", "BTC-EUR")));

        eventProcessService.processTradeEvent(executedBuyTradeEvent);

        verify(processedTradeRepository).save(any(ProcessedTrade.class));

        ArgumentCaptor<LedgerEntry> entryCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerEntryRepository).save(entryCaptor.capture());
        assertEquals(new BigDecimal("-1000.00"), entryCaptor.getValue().getCashAmount());

        ArgumentCaptor<AccountBalance> balanceCaptor = ArgumentCaptor.forClass(AccountBalance.class);
        verify(accountBalanceRepository).save(balanceCaptor.capture());
        assertEquals(new BigDecimal("2"), balanceCaptor.getValue().getPositionQuantity());
        assertEquals(new BigDecimal("-1000.00"), balanceCaptor.getValue().getCashBalance());
    }

    @Test
    @DisplayName("Should skip non EXECUTED trades")
    void shouldSkipNonExecutedTrade() {
        eventProcessService.processTradeEvent(pendingTradeEvent);

        verify(processedTradeRepository, never()).save(any());
        verify(ledgerEntryRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw on invalid trade (missing tradeId)")
    void shouldThrowOnInvalidTrade() {
    	// 1. Capture the thrown exception returned by assertThrows
    	IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
				() -> {
					eventProcessService.processTradeEvent(invalidTradeEvent);
				});
    	
    	assertEquals("tradeId is required", exception.getMessage());
    	assertThrows(IllegalArgumentException.class, 
    			() -> {
			eventProcessService.processTradeEvent(invalidTradeEvent);
		});
    }
}
