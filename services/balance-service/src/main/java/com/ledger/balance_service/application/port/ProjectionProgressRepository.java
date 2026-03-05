package com.ledger.balance_service.application.port;

import java.util.UUID;

public interface ProjectionProgressRepository {
    int getLastProcessedVersion(UUID walletId);

    void updateLastProcessedVersion(UUID walletId, int version);
}
