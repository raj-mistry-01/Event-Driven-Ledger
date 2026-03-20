package com.ledger.query_service.application.service;


import com.ledger.query_service.application.dto.response.TransactionInformation;
import com.ledger.query_service.application.dto.response.WalletSummary;
import com.ledger.query_service.application.dto.response.WalletTransactions;
import com.ledger.query_service.application.exception.TransactionNotFoundException;
import com.ledger.query_service.application.exception.WalletNotFoundException;
import com.ledger.query_service.application.port.BalanceQueryRepository;
import com.ledger.query_service.application.port.HistoryQueryRepository;
import com.ledger.query_service.application.dto.response.WalletCurrentInfoResponse;
import com.ledger.query_service.application.dto.response.WalletHistorySummary;
import org.springframework.stereotype.Service;

import java.util.Optional;
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

    public TransactionInformation getTransactionInfo(UUID transactionId) {
        return historyRepository.getTransactionInfo(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + transactionId));
    }

    public WalletTransactions getWalletTransactions(UUID walletId, String cursor, Integer limit , Integer type) {
        return historyRepository.getWalletTransactions(walletId, cursor , limit , type);
    }

    public WalletSummary getWalletSummary(UUID walletId) {
        Optional<WalletCurrentInfoResponse> walletCurrentInfo = balanceRepository.getWalletCurrentInfo(walletId);
        if (walletCurrentInfo.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found with id: " + walletId);
        }



        WalletHistorySummary historySummary = historyRepository.getWalletHistorySummary(walletId);

        return new WalletSummary(
                walletCurrentInfo.get().walletId(),
                walletCurrentInfo.get().currentBalance(),
                walletCurrentInfo.get().status(),
                historySummary.totalCredits(),
                historySummary.totalDebits(),
                historySummary.totalCreditReversals(),
                historySummary.totalDebitReversals(),
                historySummary.transactionCount(),
                historySummary.lastTransactionAt()
        );
    }

}
