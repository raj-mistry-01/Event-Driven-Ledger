package com.ledger.query_service.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletCurrentInfoResponse(
        UUID walletId,
        BigDecimal currentBalance,
        String status,
        String lastUpdatedAt
) {
}
