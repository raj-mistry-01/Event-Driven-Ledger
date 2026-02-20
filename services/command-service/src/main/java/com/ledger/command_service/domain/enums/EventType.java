package com.ledger.command_service.domain.enums;

public enum EventType {

    WALLET_CREATED(0),
    WALLET_ACTIVATED(1),
    WALLET_SUSPENDED(2),
    WALLET_CLOSED(3),
    WALLET_CREDITED(4),
    WALLET_DEBITED(5),
    CREDIT_REVERSED(6),
    DEBIT_REVERSED(7);

    private final int code;

    EventType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static EventType fromCode(int code) {
        for (EventType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event type code: " + code);
    }
}

