package com.ledger.command_service.application.port;

import java.time.Instant;
import java.util.UUID;


public record StoredEvent(
        UUID eventId,
        UUID walletId,
        int eventType,
        String eventPayload,
        int eventVersion,
        Instant eventTimestamp,
        String clientId,
        String clientRequestId
) {}
