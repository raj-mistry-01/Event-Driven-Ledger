package com.ledger.balance_service.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ledger.balance_service.application.handler.WalletCreatedHandler;
import com.ledger.balance_service.application.port.BalanceRepository;
import com.ledger.balance_service.application.port.ProjectionProgressRepository;
import com.ledger.balance_service.domain.model.WalletEvent;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BalanceProjectionService {

    private final BalanceRepository balanceRepository;
    private final ProjectionProgressRepository progressRepository;
    private final WalletCreatedHandler walletCreatedHandler;

    public BalanceProjectionService(BalanceRepository balanceRepository,
                                    ProjectionProgressRepository progressRepository,
                                    WalletCreatedHandler walletCreatedHandler) {
        this.balanceRepository = balanceRepository;
        this.progressRepository = progressRepository;
        this.walletCreatedHandler = walletCreatedHandler;
    }

    public void processEvent(WalletEvent event) {

        UUID walletId = event.walletId();

        int lastVersion = progressRepository.getLastProcessedVersion(walletId);
        int expectedVersion = lastVersion + 1;

        if (event.eventVersion() != expectedVersion) {
            return;
        }

        switch (event.eventType()) {

            case 0 -> walletCreatedHandler.handle(walletId);

            default -> throw new IllegalStateException("Unknown event type: " + event.eventType());
        }

        progressRepository.updateLastProcessedVersion(walletId, event.eventVersion());
    }

}