package com.ledger.command_service.application.port;

import org.postgresql.util.PGobject;

import java.time.Instant;
import java.util.UUID;


public record StoredEvent (
        UUID eventId,
        UUID walletId,
        int eventType,
        String eventPayload,
        int eventVersion,
        Instant eventTimestamp,
        String clientId,
        String clientRequestId
) {}
