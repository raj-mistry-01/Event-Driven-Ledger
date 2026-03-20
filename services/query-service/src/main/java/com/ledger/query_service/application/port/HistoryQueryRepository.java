package com.ledger.query_service.application.port;


import com.ledger.query_service.application.dto.response.TransactionInformation;
import com.ledger.query_service.application.dto.response.WalletTransactions;
import com.ledger.query_service.application.dto.response.WalletHistorySummary;

import java.util.Optional;
import java.util.UUID;

public interface HistoryQueryRepository {

    Optional<TransactionInformation> getTransactionInfo(UUID walletId);

    WalletTransactions getWalletTransactions(UUID walletId, String cursor, Integer limit, Integer type);

    WalletHistorySummary getWalletHistorySummary(UUID walletId);

}
