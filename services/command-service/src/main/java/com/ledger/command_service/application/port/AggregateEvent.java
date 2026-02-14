package com.ledger.command_service.application.port;

public record AggregateEvent(
        int eventType,
        String eventPayload
) {
}
