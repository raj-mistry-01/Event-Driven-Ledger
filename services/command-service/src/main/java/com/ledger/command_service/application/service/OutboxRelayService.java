package com.ledger.command_service.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.command_service.application.port.OutboxEvent;
import com.ledger.command_service.application.port.OutboxStore;
import com.ledger.command_service.infrastructure.kafka.publisher.KafkaEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

// this service is now diabled as we are using Debezium to relay events from the outbox table to Kafka

@Service
public class OutboxRelayService {

    private final OutboxStore outboxStore;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ObjectMapper objectMapper;


    public OutboxRelayService(OutboxStore outboxStore,
                              KafkaEventPublisher kafkaEventPublisher,
                              ObjectMapper objectMapper) {
        this.outboxStore = outboxStore;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.objectMapper = objectMapper;
    }

    public void processBatch(int batchSize, String topic) {
        batchSize = 50; // override batch size for testing
        List<OutboxEvent> events = outboxStore.findPending(batchSize);
        for (OutboxEvent event : events) {
            String eventAsMessage;
            try {
                eventAsMessage = objectMapper.writeValueAsString(event);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                // skip event if serialization fails
//                outboxStore.markAsFailed(event.outboxId());
//                System.out.println("Failed to serialize event " + event.outboxId() + ", skipping...");
                continue;
            }

            try {
                // publish to kafka
                kafkaEventPublisher.publish(
                        topic,
                        event.walletId().toString(),
                        eventAsMessage
                );

                // mark published after successful publish
                outboxStore.markAsPublished(event.outboxId());

            } catch (Exception ex) {

                // stop batch on failure to preserve ordering
                break;
            }
        }
    }
}