package com.ledger.command_service.application.port;

import com.ledger.command_service.domain.state.SnapshotState;

import java.util.Optional;
import java.util.UUID;


public interface SnapshotStore {

    record SnapshotWithVersion(
            UUID walletId,
            int version,
            SnapshotState state
    ) {}

    Optional<SnapshotWithVersion> load(UUID walletId) ;

    void upsert(UUID walletId, int version, SnapshotState state);

}
