package com.ledger.command_service.application.port;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(

        UUID outboxId,
        UUID eventId,
        int eventType,
        String eventPayload,
        int eventVersion,
        UUID walletId,
        int status,
        Instant createdAt,
        Instant publishedAt
) {}