package com.ledger.balance_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.ledger.balance_service.application.port.BalanceRepository;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletDebitedHandler {
    private final BalanceRepository balanceRepository;

    public WalletDebitedHandler(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public void handle(UUID walletId, JsonNode eventPayload) {
        BigDecimal amount = eventPayload.get("amount").decimalValue();
        BigDecimal currentBalance = balanceRepository.getBalance(walletId);
        BigDecimal newBalance = currentBalance.subtract(amount);
        balanceRepository.updateBalance(walletId, newBalance);
    }
}
