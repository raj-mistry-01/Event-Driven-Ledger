package com.ledger.balance_service.application.handler;

import com.ledger.balance_service.application.port.BalanceRepository;
import com.ledger.balance_service.domain.model.WalletEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletCreatedHandler implements EventHandler {

    private final BalanceRepository balanceRepository;

    public WalletCreatedHandler(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Override
    public int supportedEventType() {
        return 0;
    }

    @Override
    public void handle(WalletEvent event) {
        balanceRepository.createWallet(event.walletId());
    }
}