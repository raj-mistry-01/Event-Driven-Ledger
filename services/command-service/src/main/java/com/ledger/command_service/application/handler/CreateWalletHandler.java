package com.ledger.command_service.application.handler;

import com.ledger.command_service.application.dto.CreateWalletResponse;
import com.ledger.command_service.application.port.EventStore;
import com.ledger.command_service.application.port.OutboxStore;
import com.ledger.command_service.application.port.ProcessedCommand;
import com.ledger.command_service.application.port.ProcessedCommandStore;
import com.ledger.command_service.application.port.StoredEvent;
import com.ledger.command_service.domain.command.CreateWalletCommand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CreateWalletHandler {

    private final EventStore eventStore;
    private final OutboxStore outboxStore;
    private final ProcessedCommandStore processedCommandStore;

    public CreateWalletHandler(
            EventStore eventStore,
            OutboxStore outboxStore,
            ProcessedCommandStore processedCommandStore
    ) {
        this.eventStore = eventStore;
        this.outboxStore = outboxStore;
        this.processedCommandStore = processedCommandStore;
    }

    @Transactional
    public CreateWalletResponse handle(CreateWalletCommand command) throws Exception {

        Optional<ProcessedCommand> alreadyProcessed =
                processedCommandStore.find(
                        command.clientId(),
                        command.clientRequestId()
                );

        if (alreadyProcessed.isPresent()) {
            return new CreateWalletResponse(
                    alreadyProcessed.get().walletId(),
                    alreadyProcessed.get().eventId(),
                    "ALREADY_PROCESSED"
            );
        }


        String payload = null;

        UUID eventId = UUID.randomUUID();
        UUID walletID = UUID.randomUUID();

        StoredEvent storedEvent = new StoredEvent(
                eventId,           // eventId
                walletID,          // walletId
                1,                            // eventType (1 = WALLET_CREATED)
                payload,                      // eventPayload (json)
                1,                            // eventVersion (first event)
                Instant.now(),                // eventTimestamp
                command.clientId(),
                command.clientRequestId()
        );

        List<StoredEvent> events = List.of(storedEvent);

        eventStore.appendEvents(
                walletID,
                0,            // expectedVersion (new aggregate)
                events
        );

        outboxStore.save(events);

        processedCommandStore.markProcessed(
                command.clientId(),
                command.clientRequestId(),
                walletID,
                storedEvent.eventId()
        );

        return new CreateWalletResponse(
                walletID,
                storedEvent.eventId(),
                "CREATED"
        );
    }
}
