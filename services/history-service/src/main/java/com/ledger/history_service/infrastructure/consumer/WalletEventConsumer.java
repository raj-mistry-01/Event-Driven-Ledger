package com.ledger.history_service.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.history_service.application.service.HistoryProjectionService;
import com.ledger.history_service.domain.model.WalletEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class WalletEventConsumer {

    private final ObjectMapper objectMapper;
    private final HistoryProjectionService projectionService;

    public WalletEventConsumer(ObjectMapper objectMapper,
                               HistoryProjectionService projectionService) {
        this.objectMapper = objectMapper;
        this.projectionService = projectionService;
    }

    @KafkaListener(topics = "wallet-events", groupId = "history-projection-group")
    public void consume(String message , Acknowledgment ack) {

        try {
            WalletEvent event = objectMapper.readValue(message, WalletEvent.class);

            projectionService.processEvent(event);

            ack.acknowledge();

        } catch (Exception e) {
            throw new RuntimeException("Error occured in event proccessing ", e);
        }
    }
}
