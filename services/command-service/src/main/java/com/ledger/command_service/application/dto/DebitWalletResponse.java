package com.ledger.command_service.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DebitWalletResponse(
        UUID walletId,
        UUID transactionId,
        BigDecimal debitedAmoun,
        String status
) {
}
