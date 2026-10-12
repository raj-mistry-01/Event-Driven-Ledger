#!/bin/sh

CONNECT_URL="http://kafka-connect:8083"
CONNECTOR_NAME="ledger-outbox-connector"
CONFIG_FILE="/config/ledger-outbox-connector.json"

echo "Waiting for Kafka Connect..."

until curl -s -f "$CONNECT_URL/" > /dev/null; do
    sleep 3
done

echo "Kafka Connect is ready."

echo "Checking connector: $CONNECTOR_NAME"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    "$CONNECT_URL/connectors/$CONNECTOR_NAME")

if [ "$STATUS" = "200" ]; then
    echo "Connector already exists. Nothing to register."
    exit 0
fi

echo "Registering Debezium connector..."

curl -s -f -X POST \
    "$CONNECT_URL/connectors" \
    -H "Content-Type: application/json" \
    --data-binary "@$CONFIG_FILE"

echo
echo "Debezium connector registration completed."