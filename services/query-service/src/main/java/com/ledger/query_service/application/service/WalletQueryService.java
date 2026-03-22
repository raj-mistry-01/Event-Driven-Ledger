package com.ledger.query_service.application.service;


import com.ledger.query_service.infrastructure.cache.CacheKey;
import com.ledger.query_service.application.dto.response.TransactionInformation;
import com.ledger.query_service.application.dto.response.WalletSummary;
import com.ledger.query_service.application.dto.response.WalletTransactions;
import com.ledger.query_service.application.exception.TransactionNotFoundException;
import com.ledger.query_service.application.exception.WalletNotFoundException;
import com.ledger.query_service.application.port.BalanceQueryRepository;
import com.ledger.query_service.application.port.HistoryQueryRepository;
import com.ledger.query_service.application.dto.response.WalletCurrentInfoResponse;
import com.ledger.query_service.application.dto.response.WalletHistorySummary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletQueryService {

    private final BalanceQueryRepository balanceRepository;
    private final HistoryQueryRepository historyRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public WalletQueryService(
            BalanceQueryRepository balanceRepository,
            HistoryQueryRepository historyRepository,
            RedisTemplate<String,Object> redisTemplate
    ) {
        this.balanceRepository = balanceRepository;
        this.historyRepository = historyRepository;
        this.redisTemplate = redisTemplate;
    }



    public WalletCurrentInfoResponse getWalletCurrentInfo(UUID walletId) {
        String key = CacheKey.WALLET_CURRENT.build(walletId);
        WalletCurrentInfoResponse cached =
                (WalletCurrentInfoResponse) redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return cached;
        }

        WalletCurrentInfoResponse response =
                balanceRepository.getWalletCurrentInfo(walletId)
                        .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id" + walletId));

        redisTemplate.opsForValue().set(key, response, Duration.ofSeconds(30));

        return response;
    }

    public TransactionInformation getTransactionInfo(UUID transactionId) {
        String key = CacheKey.TRANSACTION_INFO.build(transactionId);
        TransactionInformation cached =
                (TransactionInformation) redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return cached;
        }

        TransactionInformation response = historyRepository.getTransactionInfo(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + transactionId));

        redisTemplate.opsForValue().set(key, response, Duration.ofSeconds(30));

        return response;
    }

    public WalletTransactions getWalletTransactions(UUID walletId, String cursor, Integer limit , Integer type) {
//        String key = CacheKey.WALLET_TRANSACTIONS.build(walletId, cursor, limit, type);
//        WalletTransactions cached =
//                (WalletTransactions) redisTemplate.opsForValue().get(key);

//        if (cached != null) {
//            return cached;
//        }

        WalletTransactions response = historyRepository.getWalletTransactions(walletId, cursor , limit , type);

//        redisTemplate.opsForValue().set(key, response, Duration.ofSeconds(30));

        return response;
    }

    public WalletSummary getWalletSummary(UUID walletId) {
        String key = CacheKey.WALLET_SUMMARY.build(walletId);
        WalletSummary cached =
                (WalletSummary) redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return cached;
        }

        Optional<WalletCurrentInfoResponse> walletCurrentInfo = balanceRepository.getWalletCurrentInfo(walletId);
        if (walletCurrentInfo.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found with id: " + walletId);
        }

        WalletHistorySummary historySummary = historyRepository.getWalletHistorySummary(walletId)
                ;

        WalletSummary response = new WalletSummary(
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

        redisTemplate.opsForValue().set(key, response, Duration.ofSeconds(30));

        return response;
    }

}
