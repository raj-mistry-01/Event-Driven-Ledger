package com.ledger.query_service.application.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionBaseInfo(
        UUID transactionId,
        String type,
        BigDecimal amount,
        UUID referenceTransactionId,
        Instant createdAt
) implements Serializable {
}
