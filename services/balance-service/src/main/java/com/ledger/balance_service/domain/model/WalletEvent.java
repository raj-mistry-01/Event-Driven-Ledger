package com.ledger.balance_service.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record WalletEvent(

        UUID outboxId,
        UUID eventId,
        UUID walletId,
        int eventType,
        int eventVersion,
        JsonNode eventPayload,
        Instant createdAt

) {}