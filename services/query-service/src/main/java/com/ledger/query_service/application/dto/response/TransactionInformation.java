package com.ledger.query_service.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionInformation(
        UUID transactionId,
        UUID walletId,
        String type,
        BigDecimal amount,
        UUID referenceTransactionId,
        Instant createdAt
) {}
