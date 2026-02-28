
package com.ledger.command_service.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.command_service.application.dto.AmountPayload;
import com.ledger.command_service.application.dto.CreditWalletResponse;
import com.ledger.command_service.application.dto.ReverseTransactionResponse;
import com.ledger.command_service.application.port.*;
import com.ledger.command_service.application.exception.*;
import com.ledger.command_service.domain.aggregate.WalletAggregate;
import com.ledger.command_service.domain.command.CreditWalletCommand;
import com.ledger.command_service.domain.command.ReverseTransactionCommand;
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
public class ReverseTransactionHandler {

    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final OutboxStore outboxStore;
    private final ProcessedCommandStore processedCommandStore;
    private final SnapshotPolicy snapshotPolicy;
    private final ObjectMapper objectMapper;

    public ReverseTransactionHandler(
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





        return new ReverseTransactionResponse(
                command.walletId(),
                command.originalTransactionId(),
                "o" // generate event id
        );
    }
}

