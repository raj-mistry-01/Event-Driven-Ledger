package com.ledger.query_service.controller;


import com.ledger.query_service.application.dto.response.TransactionInformation;
import com.ledger.query_service.application.dto.response.WalletSummary;
import com.ledger.query_service.application.dto.response.WalletTransactions;
import com.ledger.query_service.application.service.WalletQueryService;
import com.ledger.query_service.application.dto.response.WalletCurrentInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallet")
public class WalletQueryController {

        private final WalletQueryService walletQueryService;

        public WalletQueryController(WalletQueryService walletQueryService) {
                this.walletQueryService = walletQueryService;
        }

        @GetMapping("/currentInfo/{id}")
        public ResponseEntity<WalletCurrentInfoResponse> getWalletById(@PathVariable UUID id) {
                WalletCurrentInfoResponse response = walletQueryService.getWalletCurrentInfo(id);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/transactionInfo/{transactionId}")
        public ResponseEntity<?> getTransactionInfo(@PathVariable UUID transactionId) {
                TransactionInformation response = walletQueryService.getTransactionInfo(transactionId);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{walletId}/transactions")
        public ResponseEntity<?> getTransactionsByWalletId(
                @PathVariable UUID walletId,
                @RequestParam(defaultValue = "10") Integer limit,
                @RequestParam(required = false) String cursor,
                @RequestParam(required = false) Integer type
        ) {
                WalletTransactions reponse = walletQueryService.getWalletTransactions(walletId, cursor, limit, type);
                return ResponseEntity.ok(reponse);
        }

        @GetMapping("/{walletId}/summary")
        public ResponseEntity<WalletSummary> getWalletSummary(@PathVariable UUID walletId) {

                WalletSummary summary = walletQueryService.getWalletSummary(walletId);

                return ResponseEntity.ok(summary);
        }

}
