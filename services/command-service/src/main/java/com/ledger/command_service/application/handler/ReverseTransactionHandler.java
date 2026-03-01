
package com.ledger.command_service.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.command_service.application.dto.AmountPayload;
import com.ledger.command_service.application.dto.ReversalPayload;
import com.ledger.command_service.application.dto.ReverseTransactionResponse;
import com.ledger.command_service.application.port.*;
import com.ledger.command_service.application.exception.*;
import com.ledger.command_service.domain.aggregate.WalletAggregate;
import com.ledger.command_service.domain.command.ReverseTransactionCommand;
import com.ledger.command_service.domain.enums.EventType;
import com.ledger.command_service.domain.enums.WalletStatus;
import com.ledger.command_service.domain.state.SnapshotState;
import com.ledger.command_service.infrastructure.snapshot.SnapshotPolicy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ReverseTransactionHandler {

    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final OutboxStore outboxStore;
    private final ProcessedCommandStore processedCommandStore;
    private final SnapshotPolicy snapshotPolicy;
    private final ObjectMapper objectMapper;
    private final ReversalTracker reversalTracker;

    public ReverseTransactionHandler(
            EventStore eventStore,
            SnapshotStore snapshotStore,
            OutboxStore outboxStore,
            ProcessedCommandStore processedCommandStore,
            SnapshotPolicy snapshotPolicy,
            ObjectMapper objectMapper,
            ReversalTracker reversalTracker
    ) {
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
        this.outboxStore = outboxStore;
        this.processedCommandStore = processedCommandStore;
        this.snapshotPolicy = snapshotPolicy;
        this.objectMapper = objectMapper;
        this.reversalTracker = reversalTracker;
    }

    @Transactional
    public ReverseTransactionResponse handle(ReverseTransactionCommand command) {

        Optional<ProcessedCommand> alreadyProcessed =
                processedCommandStore.find(
                        command.clientId(),
                        command.clientRequestId()
                );

        if (alreadyProcessed.isPresent()) {
            return new ReverseTransactionResponse(
                    alreadyProcessed.get().walletId(),
                    command.originalTransactionId(),
                    alreadyProcessed.get().eventId()
            );
        }

        int currentVersion = eventStore.readCurrentVersion(command.walletId());

        if (currentVersion == 0) {
            throw new WalletNotFoundException(command.walletId().toString());
        }

        // Load snapshot
        SnapshotStore.SnapshotWithVersion snapshot =
                snapshotStore.load(command.walletId()).orElse(null);

        SnapshotState baseState =
                snapshot != null ? snapshot.state() : SnapshotState.initial();

        int snapshotVersion =
                snapshot != null ? snapshot.version() : 0;

        // Replay events
        List<AggregateEvent> events =
                eventStore.loadEventsForSnapshot(
                        command.walletId(),
                        snapshotVersion
                );

        SnapshotState currentState =
                WalletAggregate.applyEvents(baseState, events);


        //  Business validation
        if (currentState.status() == WalletStatus.CLOSED) {
            throw new WalletClosedException(command.walletId().toString());
        }

        if (currentState.status() == WalletStatus.SUSPENDED) {
            throw new WalletSuspendedException(command.walletId().toString());
        }

        // fetch original transaction event

        Optional<StoredEvent> originalEvent =
                eventStore.findByEventId(command.originalTransactionId());

        if(originalEvent.isEmpty()) {
            // thow exception
            throw new TransactionNotFoundException(command.originalTransactionId().toString());
        }

        if(originalEvent.get().eventType() != EventType.WALLET_CREDITED.code() && originalEvent.get().eventType() != EventType.WALLET_DEBITED.code()) {
            // throw exception
            throw new InvalidReversalException(command.originalTransactionId().toString());
        }

        if (!originalEvent.get().walletId().equals(command.walletId())) {
            // throw exception
            throw new InvalidReversalException(command.originalTransactionId().toString());
        }


        AmountPayload originalPayload;

        try {
            originalPayload = objectMapper.readValue(
                    originalEvent.get().eventPayload(),
                    AmountPayload.class
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to deserialize original transaction payload",
                    e
            );
        }

        BigDecimal reversalAmount = originalPayload.amount();

        EventType reversalEventType = originalEvent.get().eventType() == EventType.WALLET_CREDITED.code()
                ? EventType.CREDIT_REVERSED
                : EventType.DEBIT_REVERSED;


        if(reversalEventType == EventType.CREDIT_REVERSED) {
            if(currentState.balance().compareTo(reversalAmount) < 0) {
                throw new InsufficientFundsException(command.walletId().toString());
            }
        }

        ReversalPayload reversalPayload = new ReversalPayload(
                command.originalTransactionId(),
                reversalAmount
        );

        String reversalEventPayload;
        try {
            reversalEventPayload = objectMapper.writeValueAsString(reversalPayload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize reversal event payload", e);
        }

        // Create reversal event
        UUID reversalEventId = UUID.randomUUID();
        StoredEvent reversalEvent = new StoredEvent(
                reversalEventId,
                command.walletId(),
                reversalEventType.code(),
                reversalEventPayload,
                currentVersion + 1,
                java.time.Instant.now(),
                command.clientId(),
                command.clientRequestId()
        );


        AggregateEvent newAggregateEvent = new AggregateEvent(
                reversalEvent.eventType(),
                reversalEvent.eventPayload()
        );

        List<StoredEvent> newEvents = List.of(reversalEvent);
        List<AggregateEvent> newAggregateEventList = List.of(newAggregateEvent);
        try {
             reversalTracker.recordReversal(
                    command.originalTransactionId(),
                    reversalEventId,
                    command.walletId()
            );
        } catch (DuplicateKeyException e) {
            throw new AlreadyReversedException(command.originalTransactionId().toString());
        }
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
                reversalEventId
        );

        return new ReverseTransactionResponse(
                command.walletId(),
                command.originalTransactionId(),
                reversalEventId
        );
    }
}

