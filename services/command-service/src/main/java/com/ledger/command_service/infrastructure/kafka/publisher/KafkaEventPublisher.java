package com.ledger.command_service.infrastructure.kafka.publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String key, String payload) {
//        System.out.println("Yesss publishing message to Kafka. Topic: " + topic + ", Key: " + key);
        CompletableFuture<SendResult<String,String>> future = kafkaTemplate.send(topic, key, payload);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("Failed to publish message: " + ex.getMessage());
            } else {
                System.out.println("Message published successfully. Topic: " + topic
                        + ", Partition: " + result.getRecordMetadata().partition()
                        + ", Offset: " + result.getRecordMetadata().offset());
            }
        });
    }
}