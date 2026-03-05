package com.ledger.balance_service.application.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface BalanceRepository {
    void createWallet(UUID walletId);

    BigDecimal getBalance(UUID walletId);

    void updateBalance(UUID walletId, BigDecimal newBalance);
}
