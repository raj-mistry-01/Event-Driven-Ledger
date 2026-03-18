package com.ledger.query_service.application.service;


import com.ledger.query_service.application.exception.TransactionNotFoundException;
import com.ledger.query_service.application.exception.WalletNotFoundException;
import com.ledger.query_service.application.port.BalanceQueryRepository;
import com.ledger.query_service.application.port.HistoryQueryRepository;
import com.ledger.query_service.application.dto.response.WalletCurrentInfoResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WalletQueryService {

    private final BalanceQueryRepository balanceRepository;
    private final HistoryQueryRepository historyRepository;

    public WalletQueryService(
            BalanceQueryRepository balanceRepository,
            HistoryQueryRepository historyRepository
    ) {
        this.balanceRepository = balanceRepository;
        this.historyRepository = historyRepository;
    }


    public WalletCurrentInfoResponse getWalletCurrentInfo(UUID walletId) {
        return balanceRepository.getWalletCurrentInfo(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));
    }

}
