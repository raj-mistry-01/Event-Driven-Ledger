# Get Wallet Transactions (Filtered by Type)

## Endpoint
```
GET /wallet/{walletId}/transactions
```

## Description

Returns the transaction history of a wallet filtered by transaction type.  
This API reads from the history projection database (`wallet_history`) and supports pagination.

---

## Path Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| walletId | UUID | Yes | Unique identifier of the wallet |

---

## Query Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| type | Integer | No | Filter by transaction type (see mapping below) |
| limit | Integer | No | Number of records to return (default: 20) |
| cursor | UUID | No | Cursor for pagination |

---

## Request Example
```
GET /wallet/3fa85f64-5717-4562-b3fc-2c963f66afa6/transactions?type=4&limit=2
```

---

## Response Example (200 OK)
```json
{
  "walletId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "transactions": [
    {
      "transactionId": "txn1",
      "type": 4,
      "amount": 100.00,
      "referenceTransactionId": null,
      "createdAt": "2026-03-18T10:00:00Z"
    },
    {
      "transactionId": "txn2",
      "type": 4,
      "amount": 200.00,
      "referenceTransactionId": null,
      "createdAt": "2026-03-18T09:59:00Z"
    }
  ],
  "nextCursor": "txn2"
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| walletId | UUID | Wallet identifier |
| transactions | List | List of transaction objects |
| nextCursor | UUID | Cursor for fetching next page (or `null`) |

---

## Transaction Object

| Field | Type | Description |
|-------|------|-------------|
| transactionId | UUID | Unique transaction identifier |
| type | Integer | Transaction type (see mapping below) |
| amount | Decimal | Transaction amount |
| referenceTransactionId | UUID | Related transaction (for reversals, `null` if none) |
| createdAt | Timestamp | Transaction creation time |

---

## Transaction Type Mapping

| Value | Meaning |
|-------|---------|
| 0 | CREATED |
| 1 | ACTIVATED |
| 2 | SUSPENDED |
| 3 | CLOSED |
| 4 | CREDITED |
| 5 | DEBITED |
| 6 | CREDIT_REVERSED |
| 7 | DEBIT_REVERSED |

---

## Pagination Behavior

- Results are ordered by: `event_version DESC`
- Cursor represents: last seen `transactionId`

**Next request example:**
```
GET /wallet/{walletId}/transactions?type=4&cursor={nextCursor}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "INVALID_TRANSACTION_TYPE",
  "message": "Unsupported transaction type"
}
```

### 404 Not Found
```json
{
  "error": "WALLET_NOT_FOUND",
  "message": "Wallet does not exist"
}
```