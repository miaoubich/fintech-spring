package com.miaoubich.wallet.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.miaoubich.wallet.dto.TradeEvent;
import com.miaoubich.wallet.service.WalletProjectionService;

@Component
public class TradeEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TradeEventConsumer.class);

    private final WalletProjectionService walletProjectionService;

    public TradeEventConsumer(WalletProjectionService walletProjectionService) {
        this.walletProjectionService = walletProjectionService;
    }

    @KafkaListener(
    		topics = "${app.kafka.topics.trades:trade-events}",
            groupId = "${app.kafka.consumer.group-id:wallet-service-group}"
    )
    public void receive(@Header(KafkaHeaders.RECEIVED_KEY) String key, 
    					@Payload TradeEvent event) {
        LOG.info("Received trade event. key={}, tradeId={}, userId={}, status={}",
                key, event.tradeId(), event.userId(), event.status());

        try {
            walletProjectionService.processTradeEvent(event);
        } catch (Exception e) {
            LOG.error("Failed to project trade event to wallet. tradeId={}, error={}",
                    event.tradeId(), e.getMessage(), e);
            // Rethrowing ensures Kafka does NOT commit the offset, allowing retries
            throw e;
        }
    }
}