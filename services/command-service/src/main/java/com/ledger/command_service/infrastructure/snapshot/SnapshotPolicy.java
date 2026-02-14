package com.ledger.command_service.infrastructure.snapshot;


import com.ledger.command_service.application.port.SnapshotStore;
import com.ledger.command_service.domain.state.SnapshotState;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SnapshotPolicy {

    private final SnapshotStore snapshotStore;
    private final int snapshotInterval = 100;

    public SnapshotPolicy(
            SnapshotStore snapshotStore
    ) {
        this.snapshotStore = snapshotStore;
    }

    public void maybeSnapshot(
            UUID walletId,
            int currentVersion,
            SnapshotState snapshotState
    ) {

        if (currentVersion % snapshotInterval != 0) {
            return;
        }

        snapshotStore.upsert(
                walletId,
                currentVersion,
                snapshotState
        );
    }
}
