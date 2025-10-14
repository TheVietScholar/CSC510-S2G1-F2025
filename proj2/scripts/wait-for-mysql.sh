#!/usr/bin/env bash
set -euo pipefail

HOST="${1:-127.0.0.1}"
PORT="${2:-33060}"
TIMEOUT="${3:-60}"

echo "Waiting for MySQL at ${HOST}:${PORT} (timeout: ${TIMEOUT}s)..."
start_time=$(date +%s)
while true; do
  if timeout 1 bash -c "</dev/tcp/${HOST}/${PORT}" 2>/dev/null; then
    echo "MySQL is reachable at ${HOST}:${PORT}."
    exit 0
  fi
  now=$(date +%s)
  elapsed=$((now - start_time))
  if [[ ${elapsed} -ge ${TIMEOUT} ]]; then
    echo "Timed out after ${TIMEOUT}s waiting for MySQL at ${HOST}:${PORT}." >&2
    exit 1
  fi
  sleep 2
done
