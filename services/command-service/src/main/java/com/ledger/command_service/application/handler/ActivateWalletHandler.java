
package com.ledger.command_service.application.handler;

import com.ledger.command_service.application.port.*;
import com.ledger.command_service.application.dto.WalletLifecycleResponse;
import com.ledger.command_service.application.exception.*;
import com.ledger.command_service.domain.aggregate.WalletAggregate;
import com.ledger.command_service.domain.command.WalletLifeCycleCommand;
import com.ledger.command_service.domain.enums.WalletStatus;
import com.ledger.command_service.domain.state.SnapshotState;
import com.ledger.command_service.infrastructure.snapshot.SnapshotPolicy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ActivateWalletHandler {

    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final OutboxStore outboxStore;
    private final ProcessedCommandStore processedCommandStore;
    private final SnapshotPolicy snapshotPolicy;

    public ActivateWalletHandler(
            EventStore eventStore,
            SnapshotStore snapshotStore,
            OutboxStore outboxStore,
            ProcessedCommandStore processedCommandStore,
            SnapshotPolicy snapshotPolicy
    ) {
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
        this.outboxStore = outboxStore;
        this.processedCommandStore = processedCommandStore;
        this.snapshotPolicy = snapshotPolicy;
    }

    @Transactional
    public WalletLifecycleResponse handle(WalletLifeCycleCommand command) {

        // 1️⃣ Idempotency check
        Optional<ProcessedCommand> alreadyProcessed =
                processedCommandStore.find(
                        command.clientId(),
                        command.clientRequestId()
                );

        if (alreadyProcessed.isPresent()) {
            return new WalletLifecycleResponse(
                    alreadyProcessed.get().eventId(),
                    WalletStatus.ACTIVE.name()
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


        // 5️⃣ Business validation
        if (currentState.status() == WalletStatus.CLOSED) {
            throw new WalletClosedException(command.walletId().toString());
        }

        if (currentState.status() == WalletStatus.ACTIVE) {
            throw new WalletAlreadyActiveExcpetion();
        }


        // 6️⃣ Create WalletSuspended event
        UUID eventId = UUID.randomUUID();

        StoredEvent activatedEvent = new StoredEvent(
                eventId,
                command.walletId(),
                1, // WALLET_ACTIVATED
                null,
                currentVersion + 1,
                Instant.now(),
                command.clientId(),
                command.clientRequestId()
        );

        AggregateEvent newAggregateEvent = new AggregateEvent(
                activatedEvent.eventType(), // WALLET_ACTIVATED
                activatedEvent.eventPayload()
        );



        List<StoredEvent> newEvents = List.of(activatedEvent);
        List<AggregateEvent> newAggregateEventList = List.of(newAggregateEvent);

        // 7️⃣ Persist event
        eventStore.appendEvents(
                command.walletId(),
                currentVersion,
                newEvents
        );

        SnapshotState postState = WalletAggregate.applyEvents(currentState, newAggregateEventList);

        snapshotPolicy.maybeSnapshot(
                command.walletId(),
                currentVersion + 1,
                postState
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
                WalletStatus.ACTIVE.name()
        );
    }
}

