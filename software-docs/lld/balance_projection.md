# Balance Projection
The Balance Projection Service maintains the current wallet state (balance + status) as a read model.
By consuming the events from kafka.

## 1. Projection Consumer Flow

1. Receive event from Kafka
2. Read balance_projection_progress for wallet_id
    - If no row exists:  
      insert new row with wallet_id and last_applied_version = 0
3. Compare event_version with last_applied_version
4. If event_version <= last_applied_version:  
   → ignore event  
   → ACK Kafka message  
   → STOP processing
5. If event_version > last_applied_version + 1:  
   → do not apply event  
   → do NOT ACK (Kafka will redeliver later)  
   → STOP processing
6. BEGIN DATABASE TRANSACTION
7. Apply event to wallet table according event type
8. Update balance_projection_progress:
    - Set last_applied_version = event_version
    - (optional: update last_updated_at = now())
9. COMMIT TRANSACTION
10. ACK Kafka message

## 2. Event-Type-Specific Projection Logic

**WALLET_CREATED**  
Insert new row into wallet table:
- wallet_id
- balance = payload.initial_amount (0 if not provided)
- status = 1 (ACTIVE)

**WALLET_ACTIVATED**  
Update wallet:
- SET status = 1 (ACTIVE)

**WALLET_SUSPENDED**  
Update wallet:
- SET status = 2 (SUSPENDED)

**WALLET_CLOSED**  
Update wallet:
- SET status = 3 (CLOSED)

**WALLET_CREDITED**  
Update wallet:
- SET balance = balance + event_payload.amount

**WALLET_DEBITED**  
Update wallet:
- SET balance = balance - event_payload.amount

**TRANSACTION_REVERSED** 
Infer direction from original event type :
- If original was CREDIT:  
  SET balance = balance - event_payload.amount
- If original was DEBIT:  
  SET balance = balance + event_payload.amount

## 3. Transaction Boundary (MANDATORY)

The following operations MUST be atomic in a single database transaction:

- Apply the wallet update (INSERT or UPDATE on wallet table)
- Update balance_projection_progress (last_applied_version)

This ensures the projection remains consistent even under failures.

## 4. Failure Handling

| Scenario                          | System Behavior                                      | Outcome / Recovery Strategy                  |
|-----------------------------------|------------------------------------------------------|----------------------------------------------|
| Duplicate Kafka message           | Ignored via version check (event_version <= last_applied_version) | Safe – no duplicate application              |
| Service crash mid-update          | Transaction rolled back automatically                | Kafka redelivery → retry from same event     |
| Kafka redelivery (safe)           | Processed normally if version check passes           | Idempotent due to version tracking           |
| Out-of-order event arrival        | Event not applied; not ACKed                         | Kafka redelivery after missing event arrives |

