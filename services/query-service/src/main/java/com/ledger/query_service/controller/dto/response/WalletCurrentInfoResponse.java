package com.ledger.query_service.controller.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletCurrentInfoResponse(
        UUID walletId,
        BigDecimal currentBalance,
        String status,
        String lastUpdated
) {
}
