package com.ledger.command_service.application.port;

import java.util.List;
import java.util.UUID;

public interface EventStore {

    List<StoredEvent> loadEvents(UUID walletId);

    void appendEvents(
            UUID walletId,
            int expectedVersion,
            List<StoredEvent> newEvents
    );

    boolean walletStreamExists(UUID wallet_id);

    List<AggregateEvent> loadEventsForSnapshot(UUID walletId, int version);

}
