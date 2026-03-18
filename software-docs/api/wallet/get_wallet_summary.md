# Get Wallet Summary

## Endpoint
```
GET /wallet/{walletId}/summary
```

## Description

Returns a summary view of a wallet, including current balance and aggregated transaction statistics.  
This API reads from both:
- `balance_db` (wallet table)
- `history_db` (wallet_history table)

---

## Path Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| walletId | UUID | Yes | Unique identifier of the wallet |

---

## Query Parameters

None

---

## Request Example
```
GET /wallet/3fa85f64-5717-4562-b3fc-2c963f66afa6/summary
```

---

## Response Example (200 OK)
```json
{
  "walletId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "balance": 1200.50,
  "status": "ACTIVE",
  "totalCredits": 5000.00,
  "totalDebits": 3800.00,
  "totalCreditReversals": 200.00,
  "totalDebitReversals": 100.00,
  "transactionCount": 25,
  "lastTransactionAt": "2026-03-18T10:00:00Z"
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| walletId | UUID | Wallet identifier |
| balance | Decimal | Current wallet balance |
| status | String | Wallet status (`ACTIVE`, `SUSPENDED`, `CLOSED`) |
| totalCredits | Decimal | Sum of all credited amounts |
| totalDebits | Decimal | Sum of all debited amounts |
| totalCreditReversals | Decimal | Sum of credit reversals |
| totalDebitReversals | Decimal | Sum of debit reversals |
| transactionCount | Integer | Total number of transactions |
| lastTransactionAt | Timestamp | Timestamp of latest transaction |

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
  "error": "INVALID_WALLET_ID",
  "message": "Wallet ID format is invalid"
}
```