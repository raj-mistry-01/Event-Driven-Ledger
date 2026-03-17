package com.ledger.history_service.application.handler;

import com.ledger.history_service.domain.model.WalletEvent;

public interface EventHandler {

    int supportedEventType();

    void handle(WalletEvent event);

}
