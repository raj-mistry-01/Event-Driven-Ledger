package com.ledger.query_service.application.port;


import com.ledger.query_service.application.dto.response.WalletCurrentInfoResponse;

import java.util.Optional;
import java.util.UUID;

public interface BalanceQueryRepository  {
    Optional<WalletCurrentInfoResponse> getWalletCurrentInfo(UUID walletId);
}
