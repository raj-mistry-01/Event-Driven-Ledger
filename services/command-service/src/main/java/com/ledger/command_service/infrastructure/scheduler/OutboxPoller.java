package com.ledger.command_service.infrastructure.scheduler;

import com.ledger.command_service.application.service.OutboxRelayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


// Legacy polling-based outbox relay is disabled for the real-time architecture.
// Previously, this poller periodically queried pending events and published them to Kafka.
// Now, Debezium captures new outbox rows directly from PostgreSQL WAL and publishes them to Kafka.
// This removes polling delays and avoids the Command Service acting as the Kafka producer.
// Keeping this class for now allows easy rollback to the previous polling-based approach.


//@Component
public class OutboxPoller {

    private final OutboxRelayService relayService;

    public OutboxPoller(OutboxRelayService relayService) {
        this.relayService = relayService;
    }

    @Scheduled(fixedDelay = 50000) // every  seconds
    public void poll() {
        relayService.processBatch(100, "wallet-events");
    }
}