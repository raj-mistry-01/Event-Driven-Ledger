package com.ledger.balance_service.application.handler;

import com.ledger.balance_service.application.port.BalanceRepository;
import com.ledger.balance_service.domain.model.WalletEvent;
import com.ledger.balance_service.domain.model.WalletStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletActivatedHandler {

    private final BalanceRepository balanceRepository;

    public WalletActivatedHandler(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public void handle(UUID walletId) {
        balanceRepository.updateStatus(walletId, WalletStatus.ACTIVE.ordinal());
    }
}
