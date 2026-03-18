package com.ledger.query_service.controller;


import com.ledger.query_service.application.service.WalletQueryService;
import com.ledger.query_service.controller.dto.response.WalletCurrentInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletQueryController {

        private final WalletQueryService walletQueryService;

        public WalletQueryController(WalletQueryService walletQueryService) {
                this.walletQueryService = walletQueryService;
        }

        @GetMapping("/currentInfo/{id}")
        public ResponseEntity<WalletCurrentInfoResponse> getWalletById(@PathVariable String id) {

        }



}
