package com.ledger.history_service.application.handler;

import com.ledger.history_service.domain.model.WalletEvent;
import com.ledger.history_service.domain.model.WalletHistory;
import com.ledger.history_service.infrastructure.persistence.JdbcHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class WalletClosedHandler implements  EventHandler {

    private final JdbcHistoryRepository historyRepository;

    public WalletClosedHandler(JdbcHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }


    @Override
    public int supportedEventType() {
        return 3;
    }

    @Override
    public void handle(WalletEvent event) {
        WalletHistory history = new WalletHistory(
                null,
                event.walletId(),
                event.eventId(),
                event.eventType(),
                null,
                null,
                event.eventVersion(),
                event.createdAt()
        );

        historyRepository.insert(history);
    }

}
