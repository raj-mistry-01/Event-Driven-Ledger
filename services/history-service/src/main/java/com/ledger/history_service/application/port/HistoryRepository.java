package com.ledger.history_service.application.port;

import com.ledger.history_service.domain.model.WalletHistory;

public interface HistoryRepository {

    void insert(WalletHistory history);

}
