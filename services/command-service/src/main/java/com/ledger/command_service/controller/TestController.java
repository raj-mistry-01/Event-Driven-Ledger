package com.ledger.command_service.controller;

import com.ledger.command_service.application.service.OutboxRelayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

import com.ledger.command_service.infrastructure.kafka.publisher.KafkaEventPublisher;



// this file is just for testing the hardcoded apis

@RestController
@RequestMapping("/test")
public class TestController {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final KafkaEventPublisher kafkaEventPublisher;
    private final String topic = "wallet-events";
    private final OutboxRelayService outboxRelayService;

    public TestController(KafkaEventPublisher kafkaEventPublisher , OutboxRelayService outboxRelayService) {
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.outboxRelayService = outboxRelayService;
    }

    @Value("${server.port}")
    private String port;

    @GetMapping("/ping")
    public String ping() {
        return "Command service responding from port: " + port;
    }

    @GetMapping("/count")
    public String count() {
        int current = COUNTER.incrementAndGet();
        System.out.println("Handled by port " + port + " | count=" + current);
        return "testing";
    }

    @PostMapping("/publish")
    public String publish(@RequestBody PublishRequest request) {
        int current = COUNTER.incrementAndGet();
        /* System.out.println("yes"); */
        kafkaEventPublisher.publish(request.topic(), "test-" + current, request.message());
        return "Queued message #" + current;
    }

    @PostMapping("/testRelay")
    public String testRelay() {
        outboxRelayService.processBatch(50, topic);
        return "Triggered outbox relay";
    }

    public record PublishRequest(String topic, String message) {}
}
