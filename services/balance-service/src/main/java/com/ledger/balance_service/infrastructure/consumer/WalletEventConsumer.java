package com.ledger.balance_service.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.balance_service.domain.model.WalletEvent;
import com.ledger.balance_service.application.service.BalanceProjectionService;

import org.apache.kafka.clients.consumer.internals.Acknowledgements;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class WalletEventConsumer {

    private final ObjectMapper objectMapper;
    private final BalanceProjectionService projectionService;

    public WalletEventConsumer(ObjectMapper objectMapper,
                               BalanceProjectionService projectionService) {
        this.objectMapper = objectMapper;
        this.projectionService = projectionService;
    }

    @KafkaListener(topics = "wallet-events", groupId = "balance-projection-group")
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