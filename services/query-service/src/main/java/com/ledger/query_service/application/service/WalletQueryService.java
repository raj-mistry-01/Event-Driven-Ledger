package com.ledger.query_service.application.service;


import com.ledger.query_service.application.port.BalanceQueryRepository;
import com.ledger.query_service.application.port.HistoryQueryRepository;
import com.ledger.query_service.controller.dto.response.WalletCurrentInfoResponse;
import com.ledger.query_service.infrastructure.persistence.JdbcBalanceQueryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WalletQueryService {

    private final BalanceQueryRepository balanceRepository;

    public WalletQueryService(
            BalanceQueryRepository balanceRepository,
            HistoryQueryRepository historyRepository
    ) {
        this.balanceRepository = balanceRepository;
    }


        public WalletCurrentInfoResponse getWalletCurrentInfo(String walletId) {
//            return balanceRepository.
        }


}
