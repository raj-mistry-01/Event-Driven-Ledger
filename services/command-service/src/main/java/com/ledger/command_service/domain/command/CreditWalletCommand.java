package com.ledger.command_service.domain.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditWalletCommand(
        UUID walletId,
        BigDecimal creditAmount,
        String clientId,
        String clientRequestId
) {
}
