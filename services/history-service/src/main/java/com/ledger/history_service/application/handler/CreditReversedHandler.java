package com.ledger.history_service.application.handler;

import ch.qos.logback.core.pattern.color.WhiteCompositeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.ledger.history_service.domain.model.WalletEvent;
import com.ledger.history_service.domain.model.WalletHistory;
import com.ledger.history_service.infrastructure.persistence.JdbcHistoryRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CreditReversedHandler implements EventHandler {

    private final JdbcHistoryRepository historyRepository;

    public CreditReversedHandler(JdbcHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public int supportedEventType() {
        return 6;
    }

    @Override
    public void handle(WalletEvent event) {
        JsonNode eventPayload = event.eventPayload();
        BigDecimal amount = eventPayload.get("amount").decimalValue();
        UUID referencedEventId = UUID.fromString(eventPayload.get("original_transaction_id").asText());

        WalletHistory history = new WalletHistory(
                null,
                event.walletId(),
                event.eventId(),
                event.eventType(),
                amount,
                referencedEventId,
                event.eventVersion(),
                event.createdAt()
        );

        historyRepository.insert(history);

    }
}
