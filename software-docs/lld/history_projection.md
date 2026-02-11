# Wallet History Projection

## 1. Core Event Handling Flow

1. Receive event from Kafka
2. Read history_projection_progress for wallet_id
   - If no row exists:  
     insert new row with wallet_id and last_version = 0
3. Compare event_version with last_version
4. If event_version <= last_version:  
   → ignore event  
   → ACK Kafka message  
   → STOP processing
5. If event_version > last_applied_version + 1:  
   → do not apply event  
   → do NOT ACK (Kafka will redeliver later)  
   → STOP processing
6. BEGIN DATABASE TRANSACTION
7. Insert event into wallet_history table:
   - history_id (auto-generated)
   - event_id
   - wallet_id
   - event_type
   - event_version
   - amount
   - reference_event_id (set for reversals; NULL otherwise)
   - created_at = NOW()
8. Update history_projection_progress:
   - SET last_version = event_version
   - SET updated_at = NOW()
9. COMMIT TRANSACTION
10. ACK Kafka message

## 2. Event-Type-Specific Handling

**WALLET_CREATED**
- amount = NULL

**WALLET_ACTIVATED / WALLET_SUSPENDED / WALLET_CLOSED**
- event type is enough
- amount = NULL

**WALLET_CREDITED / WALLET_DEBITED**
- amount = event_payload.amount 

**TRANSACTION_REVERSED** (or WALLET_TRANSACTION_REVERSED)
- amount = event_payload.amount 
- reference_event_id = event_payload.original_event_id 

No balance computation or validation occurs in this projection.  
The table serves purely as an immutable audit log and transaction history.

## 3. Transaction Boundary (MANDATORY)

The following operations MUST be atomic in a single database transaction:

- INSERT into wallet_history
- UPDATE history_projection_progress (last_applied_version and updated_at)


## 4. Failure Handling

| Scenario                          | System Behavior                                      | Outcome / Recovery Strategy                  |
|-----------------------------------|------------------------------------------------------|----------------------------------------------|
| Duplicate Kafka message           | Ignored via version check (event_version <= last_applied_version) | Safe – no duplicate insertion                |
| Service crash mid-insert/update   | Transaction rolled back automatically                | Kafka redelivery → retry from same event     |
| Kafka redelivery (safe)           | Processed normally if version check passes           | Idempotent due to version tracking           |
| Out-of-order event arrival        | Event not applied; not ACKed                         | Kafka redelivery after missing event arrives |


