#!/usr/bin/env bash
# Phase 1 smoke test: produce one JSON event and consume it back.
# Run from anywhere: bash shop-lab/scripts/smoke-test.sh
set -euo pipefail

BROKER="localhost:29092"
TOPIC="order.created.v1"
KEY="ORD-SMOKE-1"
VALUE='{"eventType":"order.created","eventVersion":"1.0","data":{"orderId":"ORD-SMOKE-1","currency":"BRL","totalAmount":24990}}'

echo "--- producing 1 message (key=${KEY}) ---"
printf '%s:%s\n' "$KEY" "$VALUE" | docker exec -i shop-lab-kafka \
  kafka-console-producer --bootstrap-server "$BROKER" --topic "$TOPIC" \
  --property parse.key=true --property key.separator=:

echo "--- consuming from beginning (timeout 8s) ---"
docker exec shop-lab-kafka \
  kafka-console-consumer --bootstrap-server "$BROKER" --topic "$TOPIC" \
  --from-beginning --property print.key=true --timeout-ms 8000 2>/dev/null

echo "--- smoke test done ---"
