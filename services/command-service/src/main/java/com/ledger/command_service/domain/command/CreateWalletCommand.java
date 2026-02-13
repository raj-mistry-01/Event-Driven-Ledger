package com.ledger.command_service.domain.command;

import java.util.UUID;

public record CreateWalletCommand(
        String clientId,
        String clientRequestId
) {}
