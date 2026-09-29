package com.miaoubich.ledger.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.miaoubich.ledger.dto.TradeEvent;
import com.miaoubich.ledger.service.EventProcessService;

@Component
public class TradeEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TradeEventConsumer.class);

    private final EventProcessService eventProcessService;

    public TradeEventConsumer(EventProcessService eventProcessService) {
        this.eventProcessService = eventProcessService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.trades:trade-events}",
            groupId = "${spring.kafka.consumer.group-id:ledger-service-group}"
    	    )
    public void receive(
    		@Payload TradeEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        LOG.info(
                "Received trade event. key={}, tradeId={}, userId={}, status={}",
                key,
                event.tradeId(),
                event.userId(),
                event.status()
        );

        try {
        	eventProcessService.processTradeEvent(event);
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
