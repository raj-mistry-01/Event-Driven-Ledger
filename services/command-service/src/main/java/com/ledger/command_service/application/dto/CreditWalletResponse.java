package com.ledger.command_service.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditWalletResponse(
    UUID walletId,
    UUID transactionId,
    BigDecimal creditedAmount,
    String status
) {
}
