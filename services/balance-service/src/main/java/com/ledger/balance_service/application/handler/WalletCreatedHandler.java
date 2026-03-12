package com.ledger.balance_service.application.handler;

import com.ledger.balance_service.application.port.BalanceRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletCreatedHandler {

    private final BalanceRepository walletRepository;

    public WalletCreatedHandler(BalanceRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public void handle(UUID walletId) {
        walletRepository.createWallet(walletId);
    }
}