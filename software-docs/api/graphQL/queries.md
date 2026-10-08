# GraphQL Queries

This document describes the queries currently exposed by the Query Service GraphQL API.

All queries are sent to:

```text
POST /graphql
```

---

## 1. Get Wallet

Retrieves the current information for a wallet.

### Query

```graphql
query {
    wallet(id: "43b1a575-a7bb-4901-9c5c-25a77a64bd58") {
        walletId
        balance
        status
        lastUpdatedAt
    }
}
```

### Arguments

| Argument | Type | Required | Description |
|---|---|---|---|
| `id` | `ID` | Yes | Unique identifier of the wallet |

### Available Fields

| Field | Type | Description |
|---|---|---|
| `walletId` | `ID!` | Unique identifier of the wallet |
| `balance` | `BigDecimal!` | Current wallet balance |
| `status` | `String!` | Current wallet status |
| `lastUpdatedAt` | `String!` | Time at which the wallet projection was last updated |
| `summary` | `WalletSummary` | Aggregated wallet transaction information |
| `transactions` | `WalletTransactions` | Transaction history of the wallet |

### Example Response

```json
{
    "data": {
        "wallet": {
            "walletId": "43b1a575-a7bb-4901-9c5c-25a77a64bd58",
            "balance": "40.00",
            "status": "0",
            "lastUpdatedAt": "2026-03-19T03:07:40.760946Z"
        }
    }
}
```

---

## 2. Get Transaction

Retrieves a transaction using its transaction ID.

### Query

```graphql
query {
    transaction(id: "870ec37f-33db-43fb-b0a8-8415f0c4de9f") {
        transactionId
        walletId
        type
        amount
        referenceTransactionId
        createdAt
    }
}
```

### Arguments

| Argument | Type | Required | Description |
|---|---|---|---|
| `id` | `ID` | Yes | Unique identifier of the transaction |

### Available Fields

| Field | Type | Description |
|---|---|---|
| `transactionId` | `ID!` | Unique identifier of the transaction |
| `walletId` | `ID!` | Wallet associated with the transaction |
| `type` | `TransactionType!` | Type of transaction |
| `amount` | `BigDecimal` | Amount associated with the transaction |
| `referenceTransactionId` | `ID` | Referenced transaction for reversal operations, when applicable |
| `createdAt` | `String!` | Time at which the transaction event was created |

### Example Response

```json
{
    "data": {
        "transaction": {
            "transactionId": "870ec37f-33db-43fb-b0a8-8415f0c4de9f",
            "walletId": "43b1a575-a7bb-4901-9c5c-25a77a64bd58",
            "type": "WALLET_CREDITED",
            "amount": "100.00",
            "referenceTransactionId": null,
            "createdAt": "2026-03-19T03:07:40.760946Z"
        }
    }
}
```

---

## 3. Get Wallet Transactions

Retrieves transaction history for a wallet.

The query supports filtering by transaction type and cursor-based pagination.

### Query

```graphql
query {
    wallet(id: "43b1a575-a7bb-4901-9c5c-25a77a64bd58") {
        walletId

        transactions(limit: 10) {
            transactions {
                transactionId
                type
                amount
                referenceTransactionId
                createdAt
            }

            nextCursor
        }
    }
}
```

### Arguments

| Argument | Type | Required | Description |
|---|---|---|---|
| `limit` | `Int` | No | Maximum number of transactions to return |
| `cursor` | `String` | No | Cursor returned by a previous request for retrieving the next page |
| `type` | `TransactionType` | No | Filters transactions by transaction type |

### Transaction Types

The following values can be supplied to `type`:

```text
WALLET_CREATED
WALLET_ACTIVATED
WALLET_SUSPENDED
WALLET_CLOSED
WALLET_CREDITED
WALLET_DEBITED
CREDIT_REVERSED
DEBIT_REVERSED
```

### Filtering Example

```graphql
query {
    wallet(id: "43b1a575-a7bb-4901-9c5c-25a77a64bd58") {
        transactions(
            limit: 10
            type: WALLET_CREDITED
        ) {
            transactions {
                transactionId
                type
                amount
                createdAt
            }
            nextCursor
        }
    }
}
```

### Pagination

If another page is available, the response contains a `nextCursor`.

Use that value as the `cursor` argument of the next request.

```graphql
query {
    wallet(id: "43b1a575-a7bb-4901-9c5c-25a77a64bd58") {
        transactions(
            limit: 10
            cursor: "<next-cursor>"
        ) {
            transactions {
                transactionId
                type
                amount
                createdAt
            }
            nextCursor
        }
    }
}
```

The cursor should be treated as an opaque value by API consumers.

---

## 4. Get Wallet Summary

Retrieves aggregated transaction information for a wallet.

### Query

```graphql
query {
    wallet(id: "43b1a575-a7bb-4901-9c5c-25a77a64bd58") {
        walletId
        balance
        status

        summary {
            balance
            status
            totalCredits
            totalDebits
            totalCreditReversals
            totalDebitReversals
            transactionCount
            lastTransactionAt
        }
    }
}
```

### Summary Fields

| Field | Type | Description |
|---|---|---|
| `balance` | `BigDecimal!` | Current wallet balance |
| `status` | `String!` | Current wallet status |
| `totalCredits` | `BigDecimal!` | Total amount credited |
| `totalDebits` | `BigDecimal!` | Total amount debited |
| `totalCreditReversals` | `BigDecimal!` | Total amount of credit reversals |
| `totalDebitReversals` | `BigDecimal!` | Total amount of debit reversals |
| `transactionCount` | `Int!` | Number of transactions associated with the wallet |
| `lastTransactionAt` | `String` | Timestamp of the most recent transaction |

### Example Response

```json
{
    "data": {
        "wallet": {
            "walletId": "43b1a575-a7bb-4901-9c5c-25a77a64bd58",
            "balance": "40.00",
            "status": "0",
            "summary": {
                "balance": "40.00",
                "status": "0",
                "totalCredits": "960.00",
                "totalDebits": "0",
                "totalCreditReversals": "920.00",
                "totalDebitReversals": "0",
                "transactionCount": 4,
                "lastTransactionAt": "2026-03-19T03:07:40.760946Z"
            }
        }
    }
}
```

---

## Field Selection

GraphQL clients are not required to request every available field.

For example, a client that only requires the current balance can request:

```graphql
query {
    wallet(id: "43b1a575-a7bb-4901-9c5c-25a77a64bd58") {
        balance
    }
}
```

The response contains only the requested fields:

```json
{
    "data": {
        "wallet": {
            "balance": "40.00"
        }
    }
}
```

For the complete type definitions and API contract, see [Schema](schema.md).

For application and GraphQL execution errors, see [Errors](errors.md).