package com.ledger.query_service.application.dto.response;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record WalletTransactions(
        UUID walletId,
        List<TransactionBaseInfo> transactions,
        String nextCursor
) implements Serializable{
}
