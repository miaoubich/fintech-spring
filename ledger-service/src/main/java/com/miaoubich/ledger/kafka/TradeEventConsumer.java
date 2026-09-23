package com.miaoubich.ledger.kafka;

import com.miaoubich.ledger.dto.TradeEvent;
import com.miaoubich.ledger.service.EventProcessService;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KafkaListener(
        groupId = "${app.kafka.consumer.group-id:ledger-service-group}",
        offsetReset = OffsetReset.EARLIEST,
        offsetStrategy = OffsetStrategy.SYNC
)
public class TradeEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TradeEventConsumer.class);

    private final EventProcessService ledgerService;

    public TradeEventConsumer(EventProcessService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @Topic("${app.kafka.topics.trades:trade-events}")
    public void receive(@KafkaKey String key, TradeEvent event) {
        LOG.info(
                "Received trade event. key={}, tradeId={}, userId={}, status={}",
                key,
                event.tradeId(),
                event.userId(),
                event.status()
        );

        try {
            ledgerService.processTradeEvent(event);
        } catch (Exception e) {
            LOG.error(
                    "Failed to process trade event. tradeId={}, error={}",
                    event.tradeId(),
                    e.getMessage(),
                    e
            );

            // Rethrow so Kafka does NOT commit the offset
            // The message will be retried
            throw e;
        }
    }
}
