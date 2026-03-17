package com.ledger.history_service.application.service;

import com.ledger.history_service.application.handler.EventHandler;
import com.ledger.history_service.application.handler.EventHandlerRegistry;
import com.ledger.history_service.application.port.ProjectionProgressRepository;
import com.ledger.history_service.domain.model.WalletEvent;
import com.ledger.history_service.domain.model.WalletHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class HistoryProjectionService {

    private final ProjectionProgressRepository progressRepository;
    private final EventHandlerRegistry handlerRegistry;

    public HistoryProjectionService(ProjectionProgressRepository progressRepository,
                                    EventHandlerRegistry handlerRegistry) {
        this.progressRepository = progressRepository;
        this.handlerRegistry = handlerRegistry;
    }

    @Transactional
    public void processEvent(WalletEvent event) {

        UUID walletId = event.walletId();

        int lastVersion = progressRepository.getLastProcessedVersion(walletId);
        int expectedVersion = lastVersion + 1;

        if (event.eventVersion() != expectedVersion) {
            return;
        }

        EventHandler handler = handlerRegistry.getHandler(event.eventType());
        handler.handle(event);

        progressRepository.updateLastProcessedVersion(walletId, event.eventVersion());
    }

}