package com.ledger.command_service.application.port;

import java.util.List;
import java.util.UUID;

public interface OutboxStore {

    void save(List<StoredEvent> events);

    List<OutboxEvent> findPending(int batchSize);

    void markAsPublished(UUID outboxId);
}
