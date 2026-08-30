import requests
from concurrent.futures import ThreadPoolExecutor, as_completed
import uuid
import time

# ============================================================
# TEST 1: Concurrent Credit + Debit on the Same Wallets
#
# 2 wallets
# 500 concurrent CREDIT requests per wallet
# 500 concurrent DEBIT requests per wallet
#
# Credit amount = 100
# Debit amount  = 50
#
# IMPORTANT:
# Set INITIAL_BALANCE to the actual balance of these wallets
# before starting the test.
# ============================================================

BASE_URL = "http://localhost:8081/api/commands/wallet"

CREDIT_URL = f"{BASE_URL}/credit"
DEBIT_URL = f"{BASE_URL}/debit"

WALLET_IDS = [
    "d0bc6ae8-af84-4816-a6d3-34ae1b8ba10c",
    "c5f0144c-b3ca-465d-b7b5-e19c400627f1",
]

INITIAL_BALANCE = 100000

CREDIT_COUNT = 500
DEBIT_COUNT = 500

CREDIT_AMOUNT = 100
DEBIT_AMOUNT = 50

# Maximum number of simultaneous HTTP requests.
# Start with 20-50 and increase later for scalability testing.
MAX_WORKERS = 50


def send_credit(wallet_id):
    payload = {
        "walletId": wallet_id,
        "creditAmount": CREDIT_AMOUNT,
        "clientId": str(uuid.uuid4()),
        "clientRequestId": str(uuid.uuid4()),
    }

    start = time.perf_counter()

    try:
        response = requests.post(
            CREDIT_URL,
            json=payload,
            timeout=30
        )

        latency = time.perf_counter() - start

        return {
            "wallet_id": wallet_id,
            "operation": "CREDIT",
            "amount": CREDIT_AMOUNT,
            "status": response.status_code,
            "latency": latency,
        }

    except Exception as e:
        return {
            "wallet_id": wallet_id,
            "operation": "CREDIT",
            "amount": CREDIT_AMOUNT,
            "status": "ERROR",
            "error": str(e),
        }


def send_debit(wallet_id):
    payload = {
        "walletId": wallet_id,
        "debitAmount": DEBIT_AMOUNT,
        "clientId": str(uuid.uuid4()),
        "clientRequestId": str(uuid.uuid4()),
    }

    start = time.perf_counter()

    try:
        response = requests.post(
            DEBIT_URL,
            json=payload,
            timeout=30
        )

        latency = time.perf_counter() - start

        return {
            "wallet_id": wallet_id,
            "operation": "DEBIT",
            "amount": DEBIT_AMOUNT,
            "status": response.status_code,
            "latency": latency,
        }

    except Exception as e:
        return {
            "wallet_id": wallet_id,
            "operation": "DEBIT",
            "amount": DEBIT_AMOUNT,
            "status": "ERROR",
            "error": str(e),
        }


def run_test():
    jobs = []

    # Create 1000 jobs per wallet:
    # 500 CREDIT + 500 DEBIT
    for wallet_id in WALLET_IDS:

        for _ in range(CREDIT_COUNT):
            jobs.append(("CREDIT", wallet_id))

        for _ in range(DEBIT_COUNT):
            jobs.append(("DEBIT", wallet_id))

    print("=" * 70)
    print("TEST 1: CONCURRENT CREDIT + DEBIT")
    print("=" * 70)
    print(f"Wallets              : {len(WALLET_IDS)}")
    print(f"Requests per wallet  : {CREDIT_COUNT + DEBIT_COUNT}")
    print(f"Total requests       : {len(jobs)}")
    print(f"Credit amount        : {CREDIT_AMOUNT}")
    print(f"Debit amount         : {DEBIT_AMOUNT}")
    print(f"Initial balance      : {INITIAL_BALANCE}")
    print(f"Concurrent workers   : {MAX_WORKERS}")
    print("=" * 70)

    results = []

    test_start = time.perf_counter()

    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:

        futures = []

        for operation, wallet_id in jobs:
            if operation == "CREDIT":
                futures.append(
                    executor.submit(send_credit, wallet_id)
                )
            else:
                futures.append(
                    executor.submit(send_debit, wallet_id)
                )

        for future in as_completed(futures):
            result = future.result()
            results.append(result)

    total_time = time.perf_counter() - test_start

    print("\n" + "=" * 70)
    print("REQUEST RESULTS")
    print("=" * 70)

    for wallet_id in WALLET_IDS:

        wallet_results = [
            r for r in results
            if r["wallet_id"] == wallet_id
        ]

        successful_credits = sum(
            1 for r in wallet_results
            if r["operation"] == "CREDIT"
            and r["status"] == 200
        )

        successful_debits = sum(
            1 for r in wallet_results
            if r["operation"] == "DEBIT"
            and r["status"] == 200
        )

        failed = sum(
            1 for r in wallet_results
            if r["status"] != 200
        )

        expected_balance = (
            INITIAL_BALANCE
            + successful_credits * CREDIT_AMOUNT
            - successful_debits * DEBIT_AMOUNT
        )

        print(f"\nWallet: {wallet_id}")
        print(f"  Successful credits : {successful_credits}")
        print(f"  Successful debits  : {successful_debits}")
        print(f"  Failed requests    : {failed}")
        print(f"  Expected balance   : {expected_balance}")

    print("\n" + "=" * 70)
    print("LOAD RESULTS")
    print("=" * 70)
    print(f"Total requests : {len(results)}")
    print(f"Total time     : {total_time:.3f} seconds")
    print(f"Throughput     : {len(results) / total_time:.2f} requests/sec")
    print("=" * 70)

    print("\nIMPORTANT:")
    print("The expected balance above must be compared with the")
    print("FINAL balance in PostgreSQL AFTER Kafka projection catches up.")
    print("Do not check the balance immediately after this script finishes.")


if __name__ == "__main__":
    run_test()
