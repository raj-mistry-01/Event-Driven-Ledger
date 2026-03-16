package com.ledger.balance_service.application.handler;

import com.ledger.balance_service.application.port.BalanceRepository;
import com.ledger.balance_service.domain.model.WalletEvent;
import com.ledger.balance_service.domain.model.WalletStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletSuspendedHandler implements EventHandler{

    private final BalanceRepository balanceRepository;

    public WalletSuspendedHandler(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Override
    public int supportedEventType() {
        return 2;
    }

    @Override
    public void handle(WalletEvent event) {
        balanceRepository.updateStatus(event.walletId(), WalletStatus.SUSPENDED.ordinal());
    }
}
