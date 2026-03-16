package com.ledger.balance_service.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ledger.balance_service.application.handler.*;
import com.ledger.balance_service.application.port.BalanceRepository;
import com.ledger.balance_service.application.port.ProjectionProgressRepository;
import com.ledger.balance_service.domain.model.WalletEvent;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BalanceProjectionService {


    //    public BalanceProjectionService(BalanceRepository balanceRepository,
//                                    ProjectionProgressRepository progressRepository,
//                                    WalletCreatedHandler walletCreatedHandler,
//                                    WalletActivatedHandler walletActivatedHandler,
//                                    WalletSuspendedHandler walletSuspendedHandler,
//                                    WalletClosedHandler walletClosedHandler,
//                                    WalletCreditedHandler walletCreditedHandler,
//                                    WalletDebitedHandler walletDebitedHandler) {
//        this.progressRepository = progressRepository;
//        this.walletCreatedHandler = walletCreatedHandler;
//        this.walletActivatedHandler = walletActivatedHandler;
//        this.walletSuspendedHandler = walletSuspendedHandler;
//        this.walletClosedHandler = walletClosedHandler;
//        this.walletCreditedHandler = walletCreditedHandler;
//        this.walletDebitedHandler = walletDebitedHandler;
//    }

    // instead of larger switch use solid principles concept

        /*

            larger switch -> violates open closed principle
            violates the single repository principle

        */
//        switch (event.eventType()) {
//
//            case 0 -> walletCreatedHandler.handle(walletId);
//
//            case 1 -> walletActivatedHandler.handle(walletId);
//
//            case 2 -> walletSuspendedHandler.handle(walletId);
//
//            case 3 -> walletClosedHandler.handle(walletId);
//
//            case 4 -> {
//                JsonNode payload = event.eventPayload();
//                walletCreditedHandler.handle(walletId, payload);
//            }
//
//            case 5 -> {
//                JsonNode payload = event.eventPayload();
//                walletDebitedHandler.handle(walletId, payload);
//            }
//
//
//
//            default -> throw new IllegalStateException("Unknown event type: " + event.eventType());
//        }


    // use event registry


        /*

        SOLID principles
        Strategy pattern
        Dependency Injection
        Spring component scanning
        Clean architecture

        */





    private final ProjectionProgressRepository progressRepository;
    private final EventHandlerRegistry handlerRegistry;

    public BalanceProjectionService(ProjectionProgressRepository progressRepository,
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