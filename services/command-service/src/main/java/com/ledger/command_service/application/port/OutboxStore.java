package com.ledger.command_service.application.port;

import java.util.List;

public interface OutboxStore {

    void save(List<StoredEvent> events);
}
