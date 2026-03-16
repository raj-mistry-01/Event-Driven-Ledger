package com.ledger.command_service.infrastructure.scheduler;

import com.ledger.command_service.application.service.OutboxRelayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class OutboxPoller {

    private final OutboxRelayService relayService;

    public OutboxPoller(OutboxRelayService relayService) {
        this.relayService = relayService;
    }

    @Scheduled(fixedDelay = 5000) // every  seconds
    public void poll() {
        relayService.processBatch(100, "wallet-events");
    }
}