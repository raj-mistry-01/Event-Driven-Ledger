# Get Wallet Transactions

## Endpoint
```
GET /wallet/{walletId}/transactions
```

## Description

Returns the transaction history of a wallet, ordered by most recent events.  
This API reads from the history projection database (`wallet_history`) and supports cursor-based pagination for efficient large-scale queries.

---

## Path Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| walletId | UUID | Yes | Unique identifier of the wallet |

---

## Query Parameters

| Name | Type    | Required | Description                                                 |
|------|---------|----------|-------------------------------------------------------------|
| limit | Integer | No | Number of records to return (default: 20)                   |
| cursor | String  | No | Cursor for pagination (last seen createdAt + transactionId) |
| type | Integer | No | Filter by event type (e.g., CREDIT, DEBIT, REVERSAL)        |

---

## Request Example
```
GET /wallet/3fa85f64-5717-4562-b3fc-2c963f66afa6/transactions?limit=2
```

---

## Response Example (200 OK)
```json
{
  "walletId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "transactions": [
    {
      "transactionId": "e1",
      "eventType": "WALLET_DEBITED",
      "amount": 100.00,
      "referenceTransactionId": null,
      "createdAt": "2026-03-18T10:00:00Z"
    },
    {
      "transactionId": "e2",
      "eventType": "CREDIT_REVERSED",
      "amount": 50.00,
      "referenceTransactionId": null,
      "createdAt": "2026-03-18T09:59:00Z"
    }
  ],
  "nextCursor": "2026-03-18T09:59:00Z_e2"
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
| transactionId | UUID | Unique transaction/event identifier |
| eventType | Integer | Type of event (CREDIT, DEBIT, etc.) |
| amount | Decimal | Transaction amount |
| referenceTransactionId | UUID | Related transaction (for reversals, `null` if none) |
| createdAt | Timestamp | Event creation time |

---

## Pagination Behavior

- Results are ordered by: `event_version DESC`
- Cursor represents: last seen `createdAt + transactionId`

**Next request example:**
```
GET /wallet/{walletId}/transactions?cursor={nextCursor}
```

---

## Error Responses

### 404 Not Found
```json
{
  "error": "WALLET_NOT_FOUND",
  "message": "Wallet does not exist"
}
```

### 400 Bad Request
```json
{
  "error": "INVALID_REQUEST",
  "message": "Invalid query parameters"
}
```