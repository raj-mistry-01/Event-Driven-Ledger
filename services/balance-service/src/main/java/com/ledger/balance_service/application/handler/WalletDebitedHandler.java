package com.ledger.balance_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.ledger.balance_service.application.port.BalanceRepository;
import com.ledger.balance_service.domain.model.WalletEvent;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletDebitedHandler implements EventHandler{
    private final BalanceRepository balanceRepository;

    public WalletDebitedHandler(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Override
    public int supportedEventType() {
        return 5;
    }

    @Override
    public void handle(WalletEvent event) {
        JsonNode eventPayload = event.eventPayload();
        UUID walletId = event.walletId();
        BigDecimal amount = eventPayload.get("amount").decimalValue();
        BigDecimal currentBalance = balanceRepository.getBalance(walletId);
        BigDecimal newBalance = currentBalance.subtract(amount);
        balanceRepository.updateBalance(walletId, newBalance);
    }
}
