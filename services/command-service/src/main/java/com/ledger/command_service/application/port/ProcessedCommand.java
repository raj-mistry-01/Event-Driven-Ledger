package com.ledger.command_service.application.port;

import java.util.UUID;

/**
 * Represents the result of an already processed command.
 */
public record ProcessedCommand(
        UUID walletId,
        UUID eventId
) {}
