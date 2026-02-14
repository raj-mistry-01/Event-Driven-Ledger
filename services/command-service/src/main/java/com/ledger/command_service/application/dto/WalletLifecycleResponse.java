package com.ledger.command_service.application.dto;

import java.util.UUID;

public record WalletLifecycleResponse(
        UUID transactionId,
        String status
) {}
