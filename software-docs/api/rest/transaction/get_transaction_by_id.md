# Get Transaction by ID

## Endpoint
```
GET /wallet/transaction/{transactionId}
```

## Description

Returns the details of a specific transaction identified by its transaction ID.  
This API reads from the history projection database (`wallet_history`).

---

## Path Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| transactionId | UUID | Yes | Unique identifier of the transaction |

---

## Query Parameters

None

---

## Request Example
```
GET /wallet/transaction/e1a2b3c4-5717-4562-b3fc-2c963f66afa6
```

---

## Response Example (200 OK)
```json
{
  "transactionId": "e1a2b3c4-5717-4562-b3fc-2c963f66afa6",
  "walletId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "type": "DEBITED",
  "amount": 50.00,
  "referenceTransactionId": null,
  "createdAt": "2026-03-18T10:01:00Z"
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| transactionId | UUID | Unique transaction identifier |
| walletId | UUID | Wallet identifier |
| type | String | Transaction type (`CREDITED`, `DEBITED`, `CREDIT_REVERSED`, `DEBIT_REVERSED`, etc.) |
| amount | Decimal | Transaction amount |
| referenceTransactionId | UUID | Related transaction (for reversals, `null` if none) |
| createdAt | Timestamp | Transaction creation time |

---

## Error Responses

### 404 Not Found
```json
{
  "error": "TRANSACTION_NOT_FOUND",
  "message": "Transaction does not exist"
}
```

### 400 Bad Request
```json
{
  "error": "INVALID_TRANSACTION_ID",
  "message": "Transaction ID format is invalid"
}
```