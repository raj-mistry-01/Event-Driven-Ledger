package com.ledger.command_service.domain.command;

import java.math.BigDecimal;
import java.util.UUID;

public record DebitWalletCommand(
        UUID walletId,
        BigDecimal debitAmount,
        String clientId,
        String clientRequestId
) {
}
