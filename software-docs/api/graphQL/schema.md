# GraphQL Schema

This page describes the types, fields, arguments, enums, and scalars in the Query Service schema.

## Scalars

| Scalar | Description |
|---|---|
| `ID` | Unique identifier, sent and returned as a string |
| `String` | UTF-8 text |
| `Int` | 32-bit integer |
| `BigDecimal` | Exact decimal number, returned as a string (for example `"40.00"`) to avoid floating-point rounding |

## Root Types

| Field | Arguments | Returns |
|---|---|---|
| `wallet` | `id: ID!` | `Wallet` |
| `transaction` | `id: ID!` | `Transaction` |

## Types

### Wallet

| Field | Type | Description |
|---|---|---|
| `walletId` | `ID!` | Wallet identifier |
| `balance` | `BigDecimal!` | Current balance |
| `status` | `String!` | Current wallet status |
| `lastUpdatedAt` | `String!` | Last update timestamp |
| `summary` | `WalletSummary` | Aggregated wallet totals |
| `transactions` | `WalletTransactions` | Wallet transactions, with pagination |

**`transactions` arguments**

| Argument | Type | Description |
|---|---|---|
| `limit` | `Int` | Maximum number of transactions to return |
| `cursor` | `String` | Cursor for the next page |
| `type` | `TransactionType` | Filter by transaction type |

### Transaction

| Field | Type | Description |
|---|---|---|
| `transactionId` | `ID!` | Transaction identifier |
| `walletId` | `ID!` | Wallet the transaction belongs to |
| `type` | `TransactionType!` | Transaction type |
| `amount` | `BigDecimal` | Transaction amount |
| `referenceTransactionId` | `ID` | Related transaction, if any (for example, the original transaction for a reversal) |
| `createdAt` | `String!` | Creation timestamp |

### TransactionSummary

Same fields as `Transaction`, returned inside `WalletTransactions`.

| Field | Type | Description |
|---|---|---|
| `transactionId` | `ID!` | Transaction identifier |
| `type` | `TransactionType!` | Transaction type |
| `amount` | `BigDecimal` | Transaction amount |
| `referenceTransactionId` | `ID` | Related transaction, if any |
| `createdAt` | `String!` | Creation timestamp |

### WalletTransactions

| Field | Type | Description |
|---|---|---|
| `transactions` | `[TransactionSummary!]!` | Transactions in this page |
| `nextCursor` | `String` | Cursor for the next page, or `null` if there are no more results |

### WalletSummary

| Field | Type | Description |
|---|---|---|
| `balance` | `BigDecimal!` | Current balance |
| `status` | `String!` | Current wallet status |
| `totalCredits` | `BigDecimal!` | Sum of credits |
| `totalDebits` | `BigDecimal!` | Sum of debits |
| `totalCreditReversals` | `BigDecimal!` | Sum of credit reversals |
| `totalDebitReversals` | `BigDecimal!` | Sum of debit reversals |
| `transactionCount` | `Int!` | Number of transactions |
| `lastTransactionAt` | `String` | Timestamp of the most recent transaction |

## Enums

### TransactionType

| Value | Description |
|---|---|
| `WALLET_CREATED` | Wallet was created |
| `WALLET_ACTIVATED` | Wallet was activated |
| `WALLET_SUSPENDED` | Wallet was suspended |
| `WALLET_CLOSED` | Wallet was closed |
| `WALLET_CREDITED` | Amount was credited to the wallet |
| `WALLET_DEBITED` | Amount was debited from the wallet |
| `CREDIT_REVERSED` | A credit was reversed |
| `DEBIT_REVERSED` | A debit was reversed |