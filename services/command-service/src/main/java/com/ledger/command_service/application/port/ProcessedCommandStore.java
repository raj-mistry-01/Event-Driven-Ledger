package com.ledger.command_service.application.port;

import java.util.Optional;
import java.util.UUID;

public interface ProcessedCommandStore {


    Optional<ProcessedCommand> find(
            String clientId,
            String clientRequestId
    );

    void markProcessed(
            String clientId,
            String clientRequestId,
            UUID walletId,
            UUID eventId
    );


}
