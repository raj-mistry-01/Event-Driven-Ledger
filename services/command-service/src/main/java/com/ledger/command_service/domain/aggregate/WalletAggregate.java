package com.ledger.command_service.domain.aggregate;

import com.ledger.command_service.application.dto.AmountPayload;
import com.ledger.command_service.application.dto.ReversalPayload;
import com.ledger.command_service.domain.state.SnapshotState;
import com.ledger.command_service.domain.enums.WalletStatus;
import com.ledger.command_service.application.port.AggregateEvent;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class WalletAggregate {

    private static final ObjectMapper OBJECT_MAPPER =
            JsonMapper.builder()
                    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    .build();

    private WalletAggregate() {
        // utility class
    }


    public static SnapshotState applyEvents(
            SnapshotState baseState,
            List<AggregateEvent> events
    ) {
        SnapshotState state = baseState;

        for (AggregateEvent event : events) {
            switch (event.eventType()) {

                case 0: // WALLET_CREATED
                    state = new SnapshotState(
                            BigDecimal.ZERO,
                            WalletStatus.ACTIVE
                    );
                    break;

                case 1: // WALLET_ACTIVATED
                    state = state.withStatus(WalletStatus.ACTIVE);
                    break;

                case 2: // WALLET_SUSPENDED
                    state = state.withStatus(WalletStatus.SUSPENDED);
                    break;

                case 3: // WALLET_CLOSED
                    state = state.withStatus(WalletStatus.CLOSED);
                    break;

                case 4: // WALLET_CREDITED
                    AmountPayload creditPayload = OBJECT_MAPPER.readValue(
                            event.eventPayload(),
                            AmountPayload.class
                    );
                    state = state.withBalance(
                            state.balance().add(creditPayload.amount())
                    );
                    break;

                case 5: // WALLET_DEBITED
                    AmountPayload debitPayload = OBJECT_MAPPER.readValue(
                            event.eventPayload(),
                            AmountPayload.class
                    );
                    state = state.withBalance(
                            state.balance().subtract(debitPayload.amount())
                    );
                    break;

                case 6 : // CREDIT_REVERSED
                    ReversalPayload creditReversalPayload = OBJECT_MAPPER.readValue(
                            event.eventPayload(),
                            ReversalPayload.class
                    );
                    state = state.withBalance(
                            state.balance().subtract(creditReversalPayload.amount())
                    );
                    break;

                case 7 : // DEBIT_REVERSED
                    ReversalPayload debitReversalPayload = OBJECT_MAPPER.readValue(
                            event.eventPayload(),
                            ReversalPayload.class
                    );
                    state = state.withBalance(
                            state.balance().add(debitReversalPayload.amount())
                    );
                    break;

                default:
                    throw new IllegalStateException(
                            "Unknown event type: " + event.eventType()
                    );
            }
        }

        return state;
    }
}
