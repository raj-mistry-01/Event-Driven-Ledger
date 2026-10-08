# GraphQL Errors

GraphQL execution errors are returned in the `errors` field of the response. A failed field is `null` in `data`.

## Error Format

| Field | Description |
|---|---|
| `message` | Human-readable description |
| `path` | Field where the error occurred |
| `locations` | Position of the field in the query |
| `extensions.code` | Application error code (use this in client logic) |
| `extensions.classification` | General error category |

Example:

```json
{
  "errors": [
    {
      "message": "Wallet with id 870ec37f-33db-43fb-b0a8-8415f0c4de9e not found.",
      "path": ["wallet"],
      "extensions": {
        "code": "WALLET_NOT_FOUND",
        "classification": "NOT_FOUND"
      }
    }
  ],
  "data": {
    "wallet": null
  }
}
```

## Error Codes

| Code | Classification | Description |
|---|---|---|
| `WALLET_NOT_FOUND` | `NOT_FOUND` | Wallet does not exist |
| `TRANSACTION_NOT_FOUND` | `NOT_FOUND` | Transaction does not exist |
| `INVALID_REQUEST` | `BAD_REQUEST` | Request contains invalid input |
| `INTERNAL_SERVER_ERROR` | `INTERNAL_ERROR` | Unexpected server error (generic message returned) |

## HTTP Status

GraphQL errors are returned with `HTTP 200 OK`, because the request was processed. Check the `errors` field, not the HTTP status, to detect failures.

## Handling Errors

Clients should branch on `extensions.code`, not on the `message` text, since the message is for humans and may change.

```javascript
const code = response.errors?.[0]?.extensions?.code;

if (code === "WALLET_NOT_FOUND") {
  // handle missing wallet
}
```

Use `path` to find which field failed when a query requests several fields. Other fields may still return data.