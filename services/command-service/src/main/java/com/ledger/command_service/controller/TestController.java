package com.ledger.command_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

import com.ledger.command_service.infrastructure.kafka.publisher.KafkaEventPublisher;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final KafkaEventPublisher kafkaEventPublisher;

    public TestController(KafkaEventPublisher kafkaEventPublisher) {
        this.kafkaEventPublisher = kafkaEventPublisher;
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

    public record PublishRequest(String topic, String message) {}
}
