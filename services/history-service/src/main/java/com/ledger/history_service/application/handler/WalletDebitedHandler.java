package com.ledger.history_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.ledger.history_service.domain.model.WalletEvent;
import com.ledger.history_service.domain.model.WalletHistory;
import com.ledger.history_service.infrastructure.persistence.JdbcHistoryRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class WalletDebitedHandler implements EventHandler {

    private final JdbcHistoryRepository historyRepository;

    public WalletDebitedHandler(JdbcHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public int supportedEventType() {
        return 5;
    }

    @Override
    public void handle(WalletEvent event) {
        JsonNode eventPayload = event.eventPayload();
        BigDecimal amount = eventPayload.get("amount").decimalValue();
        WalletHistory history = new WalletHistory(
                null,
                event.walletId(),
                event.eventId(),
                event.eventType(),
                amount,
                null,
                event.eventVersion(),
                event.createdAt()
        );
        historyRepository.insert(history);

    }
}
