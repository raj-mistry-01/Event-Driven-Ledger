# GraphQL API

The Query Service provides a GraphQL API for reading wallet and transaction data from the ledger read model. Clients request only the fields they need.

## Endpoint

All operations are sent as `POST /graphql`.

## Supported Operations

| Query | Description |
|---|---|
| `wallet(id)` | Wallet details, with nested summary and transactions |
| `transaction(id)` | A single transaction |

The `wallet` query also supports transaction filtering and cursor-based pagination.

## Request Example

```json
{
  "query": "query { wallet(id: \"<wallet-id>\") { walletId balance status } }"
}
```

## Response Example

```json
{
  "data": {
    "wallet": {
      "walletId": "<wallet-id>",
      "balance": "40.00",
      "status": "0"
    }
  }
}
```

Execution errors are returned in the `errors` field. See [Errors](errors.md).

## Documentation

- [Schema](schema.md): types, fields, arguments, enums, and scalars
- [Queries](queries.md): queries with arguments and return types
- [Errors](errors.md): error handling and error codes
- [Examples](examples.md): sample queries and responses