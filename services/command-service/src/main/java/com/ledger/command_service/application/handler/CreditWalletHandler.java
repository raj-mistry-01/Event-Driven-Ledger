
package com.ledger.command_service.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.command_service.application.dto.AmountPayload;
import com.ledger.command_service.application.dto.CreditWalletResponse;
import com.ledger.command_service.application.port.*;
import com.ledger.command_service.application.exception.*;
import com.ledger.command_service.domain.aggregate.WalletAggregate;
import com.ledger.command_service.domain.command.CreditWalletCommand;
import com.ledger.command_service.domain.enums.EventType;
import com.ledger.command_service.domain.enums.WalletStatus;
import com.ledger.command_service.domain.state.SnapshotState;
import com.ledger.command_service.infrastructure.snapshot.SnapshotPolicy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CreditWalletHandler {

    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final OutboxStore outboxStore;
    private final ProcessedCommandStore processedCommandStore;
    private final SnapshotPolicy snapshotPolicy;
    private final ObjectMapper objectMapper;

    public CreditWalletHandler(
            EventStore eventStore,
            SnapshotStore snapshotStore,
            OutboxStore outboxStore,
            ProcessedCommandStore processedCommandStore,
            SnapshotPolicy snapshotPolicy,
            ObjectMapper objectMapper
    ) {
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
        this.outboxStore = outboxStore;
        this.processedCommandStore = processedCommandStore;
        this.snapshotPolicy = snapshotPolicy;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CreditWalletResponse handle(CreditWalletCommand command) {
        // 1️⃣ Idempotency check
        Optional<ProcessedCommand> alreadyProcessed =
                processedCommandStore.find(
                        command.clientId(),
                        command.clientRequestId()
                );

        if (alreadyProcessed.isPresent()) {
            return new CreditWalletResponse(
                    alreadyProcessed.get().walletId(),
                    alreadyProcessed.get().eventId(),
                    command.creditAmount(),
                    WalletStatus.CREDITED.name()
            );
        }



        if(command.creditAmount() == null || command.creditAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
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

        if (currentState.status() == WalletStatus.SUSPENDED) {
            throw new WalletSuspendedException(command.walletId().toString());
        }


        // 6️⃣ Create WalletCredit event
        UUID eventId = UUID.randomUUID();
        AmountPayload payload = new AmountPayload(command.creditAmount());
        String eventPayload;
        try{
            eventPayload = objectMapper.writeValueAsString(payload);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }


        StoredEvent creditEvent = new StoredEvent(
                eventId,
                command.walletId(),
                EventType.WALLET_CREDITED.code(), // WALLET_CREDITED
                eventPayload,
                currentVersion + 1,
                Instant.now(),
                command.clientId(),
                command.clientRequestId()
        );


        AggregateEvent newAggregateEvent = new AggregateEvent(
                creditEvent.eventType(), // WALLET_CREDITED
                creditEvent.eventPayload()
        );

        List<StoredEvent> newEvents = List.of(creditEvent);
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

        return new CreditWalletResponse(
                command.walletId(),
                eventId,
                command.creditAmount(),
                WalletStatus.CREDITED.name()
        );
    }
}

