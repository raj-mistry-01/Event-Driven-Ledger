package com.ledger.balance_service.application.handler;

import com.ledger.balance_service.application.port.BalanceRepository;
import com.ledger.balance_service.domain.model.WalletStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletSuspendedHandler {

    private final BalanceRepository walletRepository;

    public WalletSuspendedHandler(BalanceRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public void handle(UUID walletId) {
        walletRepository.updateStatus(walletId, WalletStatus.SUSPENDED.ordinal()); // 2 for suspended
    }

}
