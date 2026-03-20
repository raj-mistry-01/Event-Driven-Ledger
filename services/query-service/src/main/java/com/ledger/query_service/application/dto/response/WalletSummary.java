package com.ledger.query_service.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletSummary (
        UUID walletId,
        BigDecimal balance,
        String status,
        BigDecimal totalCredits,
        BigDecimal totalDebits,
        BigDecimal totalCreditReversals,
        BigDecimal totalDebitReversals,
        int transactionCount,
        Instant lastTransactionAt
){
}
