package com.ledger.query_service.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletHistorySummary(
        BigDecimal totalCredits,
        BigDecimal totalDebits,
        BigDecimal totalCreditReversals,
        BigDecimal totalDebitReversals,
        int transactionCount,
        Instant lastTransactionAt
) {
}
