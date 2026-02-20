package com.ledger.command_service.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DebitWalletRequest(
        UUID walletId,
        BigDecimal debitAmount,
        String clientId,
        String clientRequestId
) {
}

