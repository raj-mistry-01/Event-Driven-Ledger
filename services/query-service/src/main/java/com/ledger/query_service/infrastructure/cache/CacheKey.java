package com.ledger.query_service.infrastructure.cache;

public enum CacheKey {
    WALLET_CURRENT("wallet:current"),
    WALLET_SUMMARY("wallet:summary"),
    WALLET_TRANSACTIONS("wallet:transactions"),
    TRANSACTION_INFO("transaction:info");

    private final String prefix;

    CacheKey(String prefix) {
        this.prefix = prefix;
    }

    public String build(Object... parts) {
        StringBuilder builder = new StringBuilder(prefix);
        for (Object part : parts) {
            builder.append(':').append(String.valueOf(part));
        }
        return builder.toString();
    }
}
