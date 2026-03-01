package com.ledger.command_service.application.port;

import java.util.UUID;


public interface ReversalTracker {

    void recordReversal(
            UUID originalTransactionId,
            UUID reversalEventId,
            UUID walletId
    );
}