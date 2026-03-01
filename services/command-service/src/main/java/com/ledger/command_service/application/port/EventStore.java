package com.ledger.command_service.application.port;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public interface EventStore {

    List<StoredEvent> loadEvents(UUID walletId);

    void appendEvents(
            UUID walletId,
            int expectedVersion,
            List<StoredEvent> newEvents
    );


    List<AggregateEvent> loadEventsForSnapshot(UUID walletId, int version);

    int readCurrentVersion(UUID uuid);

    Optional<StoredEvent> findByEventId(UUID eventId);
}
