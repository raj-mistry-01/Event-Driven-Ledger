package com.ledger.command_service.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditWalletRequest(
        UUID walletId,
        BigDecimal creditAmount,
        String clientId,
        String clientRequestId
) {
}
