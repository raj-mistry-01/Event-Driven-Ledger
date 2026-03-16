package com.ledger.balance_service.application.handler;

import com.ledger.balance_service.application.port.BalanceRepository;
import com.ledger.balance_service.domain.model.WalletStatus;

import java.util.UUID;

public class WalletClosedHandler {

    private final BalanceRepository balanceRepository;

    public WalletClosedHandler(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public void handle(UUID walletId) {
        balanceRepository.updateStatus(walletId, WalletStatus.CLOSED.ordinal());
    }

}
