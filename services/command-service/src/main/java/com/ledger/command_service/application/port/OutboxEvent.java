package com.ledger.command_service.application.port;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(

        UUID outboxId,
        UUID eventId,
        int eventType,
        JsonNode eventPayload,
        int eventVersion,
        UUID walletId,
        Instant createdAt
) {}