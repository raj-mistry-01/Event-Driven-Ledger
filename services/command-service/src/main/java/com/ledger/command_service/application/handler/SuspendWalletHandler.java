
package com.ledger.command_service.application.handler;

import com.ledger.command_service.application.port.*;
import com.ledger.command_service.application.dto.WalletLifecycleResponse;
import com.ledger.command_service.application.exception.*;
import com.ledger.command_service.domain.aggregate.WalletAggregate;
import com.ledger.command_service.domain.command.SuspendWalletCommand;
import com.ledger.command_service.domain.enums.WalletStatus;
import com.ledger.command_service.domain.state.SnapshotState;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SuspendWalletHandler {

    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final OutboxStore outboxStore;
    private final ProcessedCommandStore processedCommandStore;

    public SuspendWalletHandler(
            EventStore eventStore,
            SnapshotStore snapshotStore,
            OutboxStore outboxStore,
            ProcessedCommandStore processedCommandStore
    ) {
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
        this.outboxStore = outboxStore;
        this.processedCommandStore = processedCommandStore;
    }

    @Transactional
    public WalletLifecycleResponse handle(SuspendWalletCommand command) {

        // 1️⃣ Idempotency check
        Optional<ProcessedCommand> alreadyProcessed =
                processedCommandStore.find(
                        command.clientId(),
                        command.clientRequestId()
                );

        if (alreadyProcessed.isPresent()) {
            return new WalletLifecycleResponse(
                    alreadyProcessed.get().eventId(),
                    WalletStatus.SUSPENDED.name()
            );
        }

        // 2️⃣ Wallet existence + current version
        int currentVersion = eventStore.readCurrentVersion(command.walletId());

        if (currentVersion == 0) {
            throw new WalletNotFoundException(command.walletId().toString());
        }

        // 3️⃣ Load snapshot
        SnapshotStore.SnapshotWithVersion snapshot =
                snapshotStore.load(command.walletId()).orElse(null);

        SnapshotState baseState =
                snapshot != null ? snapshot.state() : SnapshotState.initial();

        int snapshotVersion =
                snapshot != null ? snapshot.version() : 0;

        // 4️⃣ Replay events
        List<AggregateEvent> events =
                eventStore.loadEventsForSnapshot(
                        command.walletId(),
                        snapshotVersion
                );

        SnapshotState currentState =
                WalletAggregate.applyEvents(baseState, events);

        System.out.println(currentState.status());

        // 5️⃣ Business validation
        if (currentState.status() == WalletStatus.CLOSED) {
            throw new WalletClosedException(command.walletId().toString());
        }

        if (currentState.status() == WalletStatus.SUSPENDED) {
            throw new WalletAlreadySuspendedException();
        }

        // 6️⃣ Create WalletSuspended event
        UUID eventId = UUID.randomUUID();

        StoredEvent suspendedEvent = new StoredEvent(
                eventId,
                command.walletId(),
                2, // WALLET_SUSPENDED
                null,
                currentVersion + 1,
                Instant.now(),
                command.clientId(),
                command.clientRequestId()
        );

        List<StoredEvent> newEvents = List.of(suspendedEvent);

        // 7️⃣ Persist event
        eventStore.appendEvents(
                command.walletId(),
                currentVersion,
                newEvents
        );

        // 8️⃣ Outbox
        outboxStore.save(newEvents);

        // 9️⃣ Mark command processed
        processedCommandStore.markProcessed(
                command.clientId(),
                command.clientRequestId(),
                command.walletId(),
                eventId
        );

        return new WalletLifecycleResponse(
                eventId,
                WalletStatus.SUSPENDED.name()
        );
    }
}

