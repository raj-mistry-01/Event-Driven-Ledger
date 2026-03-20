# Get Wallet Current Info

## Endpoint
```
GET /wallet/currentInfo/{walletId}
```

## Description

Returns the current state of a wallet, including balance, status, and last updated timestamp.  
This API reads from the balance projection database.

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
GET /wallet/currentInfo/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

---

## Response Example (200 OK)
```json
{
  "walletId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "balance": 1200.50,
  "status": "ACTIVE",
  "lastUpdatedAt": "2026-03-18T10:00:00Z"
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| walletId | UUID | Wallet identifier |
| balance | Decimal | Current wallet balance |
| status | String | Wallet status (`ACTIVE`, `SUSPENDED`, `CLOSED`) |
| lastUpdatedAt | Timestamp | Last update time of wallet |

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