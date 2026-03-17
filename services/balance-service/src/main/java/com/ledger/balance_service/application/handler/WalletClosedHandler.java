package com.ledger.balance_service.application.handler;

import com.ledger.balance_service.application.port.BalanceRepository;
import com.ledger.balance_service.domain.model.WalletEvent;
import com.ledger.balance_service.domain.enums.WalletStatus;
import org.springframework.stereotype.Component;

@Component
public class WalletClosedHandler implements EventHandler{

    private final BalanceRepository balanceRepository;

    public WalletClosedHandler(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Override
    public int supportedEventType() {
        return 3;
    }

    @Override
    public void handle(WalletEvent event) {
        balanceRepository.updateStatus(event.walletId(), WalletStatus.CLOSED.ordinal());
    }
}
