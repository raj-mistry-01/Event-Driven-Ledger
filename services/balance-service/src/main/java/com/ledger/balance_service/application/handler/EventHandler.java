package com.ledger.balance_service.application.handler;

import com.ledger.balance_service.domain.model.WalletEvent;

public interface EventHandler {

    int supportedEventType();

    void handle(WalletEvent event);

}