# Optimistic Concurrency Retry 

This LLD applies to **all command-side operations** that:

- Append one or more domain events
- Advance `wallet_stream_head`
- Assign `event_version`

## Optimistic Concurrency Failure

Occurs when another transaction has already updated `wallet_stream_head` and the expected last version no longer matches.

Detected via:

```sql
UPDATE wallet_stream_head
SET last_version = new_version
WHERE wallet_id = ?
  AND last_version = expected_version;
```

If 0 rows are updated → optimistic concurrency conflict (contention, not an error)

```
attempt = 0

while attempt < MAX_RETRIES:

    1. Read current stream head
       - SELECT last_version FROM wallet_stream_head WHERE wallet_id = ?
       - Set expected_version = last_version

    2. Load wallet aggregate
       - Load latest snapshot (if snapshotting is used)
       - Replay events from snapshot version + 1 to expected_version

    3. Validate business rules
       - Wallet state checks
       - Amount constraints
       - Other domain invariants

    4. Prepare new domain event(s)
       - event_version = expected_version + 1 (or +N if multiple events)

    5. BEGIN DATABASE TRANSACTION

    6. Persist event(s) into events table

    7. Attempt to advance stream head 

    8. Check rows affected:
       - If rows updated = 1:
           success
          other flow

       - If rows updated = 0:
           rollback

end while
```

**Error Reponse** : if all retry attempts fail
```json
{
  "error": "SERVICE_UNAVAILABLE",
  "message": "Please retry your request."
}
```