package com.ledger.balance_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.ledger.balance_service.application.port.BalanceRepository;
import com.ledger.balance_service.domain.model.WalletEvent;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletCreditedHandler {

    private final BalanceRepository balanceRepository;


    public WalletCreditedHandler(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public void handle(UUID walletId, JsonNode eventPayload) {
        BigDecimal amount = eventPayload.get("amount").decimalValue();
        BigDecimal currentBalance = balanceRepository.getBalance(walletId);
        BigDecimal newBalance = currentBalance.add(amount);
        balanceRepository.updateBalance(walletId, newBalance);
    }

}
