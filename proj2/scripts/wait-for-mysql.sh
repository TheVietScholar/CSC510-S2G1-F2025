#!/usr/bin/env bash
set -euo pipefail

HOST="${1:-127.0.0.1}"
PORT="${2:-3307}"
TIMEOUT="${3:-120}"
USER="${4:-app}"
PASS="${5:-app}"

echo "Waiting for MySQL at ${HOST}:${PORT} (timeout: ${TIMEOUT}s)..."
start=$(date +%s)
while true; do
  if mysqladmin ping -h "$HOST" -P "$PORT" -u "$USER" -p"$PASS" --silent >/dev/null 2>&1; then
    echo "MySQL is ready at ${HOST}:${PORT}."
    exit 0
  fi
  now=$(date +%s); elapsed=$((now - start))
  if [ "$elapsed" -ge "$TIMEOUT" ]; then
    echo "Timed out after ${TIMEOUT}s waiting for MySQL at ${HOST}:${PORT}." >&2
    exit 1
  fi
  sleep 2
done