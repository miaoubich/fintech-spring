package com.miaoubich.wallet.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miaoubich.wallet.dto.TradeEvent;
import com.miaoubich.wallet.service.WalletProjectionService;

import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaListener(
        groupId = "${app.kafka.consumer.group-id:wallet-service-group}",
        offsetReset = OffsetReset.EARLIEST,
        offsetStrategy = OffsetStrategy.SYNC
)
public class TradeEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TradeEventConsumer.class);

    private final WalletProjectionService walletProjectionService;

    public TradeEventConsumer(WalletProjectionService walletProjectionService) {
        this.walletProjectionService = walletProjectionService;
    }

    @Topic("${app.kafka.topics.trades:trade-events}")
    public void receive(@KafkaKey String key, TradeEvent event) {
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