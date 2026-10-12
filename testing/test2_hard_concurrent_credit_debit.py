import requests
from concurrent.futures import ThreadPoolExecutor, as_completed
import uuid
import time
from collections import Counter

# ============================================================
# TEST 2: HARDER CONCURRENT CREDIT + DEBIT TEST
#
# 2 wallets
# 5,000 requests per wallet
# 10,000 total requests
# 200 concurrent HTTP workers
#
# This test is intentionally harder than Test 1.
#
# IMPORTANT:
# INITIAL_BALANCE must match the actual starting balance
# of BOTH wallets before running the test.
#
# The expected balance is calculated from the ACTUAL
# successful API responses, not from request order.
# ============================================================

BASE_URL = "http://localhost:8081/api/commands/wallet"

CREDIT_URL = f"{BASE_URL}/credit"
DEBIT_URL = f"{BASE_URL}/debit"

WALLET_IDS = [
    "ba629a4f-b476-4480-9e05-51695d0ae093",
    "b9d50909-8bd0-4713-aff3-63764d450829",
]

INITIAL_BALANCE = 100000

# 5,000 operations per wallet
CREDIT_COUNT = 2500
DEBIT_COUNT = 2500

CREDIT_AMOUNT = 100
DEBIT_AMOUNT = 50

# Much higher parallelism than Test 1
MAX_WORKERS = 200

REQUEST_TIMEOUT = 30


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
            timeout=REQUEST_TIMEOUT
        )

        return {
            "wallet_id": wallet_id,
            "operation": "CREDIT",
            "amount": CREDIT_AMOUNT,
            "status": response.status_code,
            "latency": time.perf_counter() - start,
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
            timeout=REQUEST_TIMEOUT
        )

        return {
            "wallet_id": wallet_id,
            "operation": "DEBIT",
            "amount": DEBIT_AMOUNT,
            "status": response.status_code,
            "latency": time.perf_counter() - start,
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

    for wallet_id in WALLET_IDS:

        for _ in range(CREDIT_COUNT):
            jobs.append(("CREDIT", wallet_id))

        for _ in range(DEBIT_COUNT):
            jobs.append(("DEBIT", wallet_id))

    print("=" * 75)
    print("TEST 2: HARD CONCURRENT CREDIT + DEBIT")
    print("=" * 75)
    print(f"Wallets              : {len(WALLET_IDS)}")
    print(f"Requests per wallet  : {CREDIT_COUNT + DEBIT_COUNT}")
    print(f"Total requests       : {len(jobs)}")
    print(f"Credit requests      : {CREDIT_COUNT * len(WALLET_IDS)}")
    print(f"Debit requests       : {DEBIT_COUNT * len(WALLET_IDS)}")
    print(f"Credit amount        : {CREDIT_AMOUNT}")
    print(f"Debit amount         : {DEBIT_AMOUNT}")
    print(f"Initial balance      : {INITIAL_BALANCE}")
    print(f"Concurrent workers   : {MAX_WORKERS}")
    print("=" * 75)

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

        completed = 0

        for future in as_completed(futures):
            result = future.result()
            results.append(result)

            completed += 1

            if completed % 1000 == 0:
                print(f"Completed {completed}/{len(jobs)} requests")

    total_time = time.perf_counter() - test_start

    print("\n" + "=" * 75)
    print("REQUEST RESULTS")
    print("=" * 75)

    overall_statuses = Counter(
        str(r["status"]) for r in results
    )

    print("\nOverall HTTP status distribution:")

    for status, count in sorted(overall_statuses.items()):
        print(f"  {status}: {count}")

    for wallet_id in WALLET_IDS:

        wallet_results = [
            r for r in results
            if r["wallet_id"] == wallet_id
        ]

        credits_success = sum(
            1 for r in wallet_results
            if r["operation"] == "CREDIT"
            and r["status"] == 200
        )

        debits_success = sum(
            1 for r in wallet_results
            if r["operation"] == "DEBIT"
            and r["status"] == 200
        )

        status_counts = Counter(
            str(r["status"]) for r in wallet_results
        )

        expected_balance = (
            INITIAL_BALANCE
            + credits_success * CREDIT_AMOUNT
            - debits_success * DEBIT_AMOUNT
        )

        print("\n" + "-" * 75)
        print(f"Wallet: {wallet_id}")
        print("-" * 75)

        print(f"  Successful credits : {credits_success}")
        print(f"  Successful debits  : {debits_success}")
        print(f"  Failed requests    : {len(wallet_results) - credits_success - debits_success}")

        print("  Status distribution:")

        for status, count in sorted(status_counts.items()):
            print(f"    {status}: {count}")

        print(f"  Expected balance   : {expected_balance}")

    print("\n" + "=" * 75)
    print("LOAD RESULTS")
    print("=" * 75)
    print(f"Total requests : {len(results)}")
    print(f"Total time     : {total_time:.3f} seconds")
    print(f"Throughput     : {len(results) / total_time:.2f} requests/sec")
    print("=" * 75)

    print("\nNEXT CORRECTNESS CHECK:")
    print("1. Wait for Kafka/balance projection to catch up.")
    print("2. Query the final balance of both wallets.")
    print("3. Compare DB balance with the Expected balance above.")
    print("4. Also compare successful command/event counts.")
    print("\nDo NOT treat HTTP failures as scalability failures until")
    print("we inspect their status codes and business meaning.")


if __name__ == "__main__":
    run_test()
