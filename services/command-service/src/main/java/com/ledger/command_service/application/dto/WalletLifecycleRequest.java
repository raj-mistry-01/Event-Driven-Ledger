package com.ledger.command_service.application.dto;

import java.util.UUID;

public record WalletLifecycleRequest(
        UUID walletId,
        String clientId,
        String clientRequestId
) {}
