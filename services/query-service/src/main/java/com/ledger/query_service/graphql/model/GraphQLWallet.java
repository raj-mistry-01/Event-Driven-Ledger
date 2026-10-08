package com.ledger.query_service.graphql.model;

import java.math.BigDecimal;
import java.util.UUID;

public record GraphQLWallet(
        UUID walletId,
        BigDecimal balance,
        String status,
        String lastUpdatedAt
) {
}