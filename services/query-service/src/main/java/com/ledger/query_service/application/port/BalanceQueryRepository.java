package com.ledger.query_service.application.port;


import com.ledger.query_service.application.dto.response.TransactionBaseInfo;
import com.ledger.query_service.application.dto.response.WalletCurrentInfoResponse;
import com.ledger.query_service.application.dto.response.WalletTransactions;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BalanceQueryRepository  {

    Optional<WalletCurrentInfoResponse> getWalletCurrentInfo(UUID walletId);


}
