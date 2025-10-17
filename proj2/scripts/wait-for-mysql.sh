#!/usr/bin/env bash
set -euo pipefail

HOST="${1:-127.0.0.1}"
PORT="${2:-3307}"
TIMEOUT="${3:-60}"

# Tunables (env overrides):
# - CHECK_INTERVAL: seconds between checks
# - PER_ATTEMPT_TIMEOUT: connection attempt timeout (seconds)
CHECK_INTERVAL="${CHECK_INTERVAL:-2}"
PER_ATTEMPT_TIMEOUT="${PER_ATTEMPT_TIMEOUT:-1}"

echo "Waiting for MySQL at ${HOST}:${PORT} (timeout: ${TIMEOUT}s)..."

check_conn() {
  local host="$1" port="$2" per_timeout="$3"

  # Prefer GNU timeout if available
  if command -v timeout >/dev/null 2>&1; then
    timeout "${per_timeout}" bash -c "</dev/tcp/${host}/${port}" >/dev/null 2>&1
    return $?
  fi

  # Homebrew coreutils on macOS provides gtimeout
  if command -v gtimeout >/dev/null 2>&1; then
    gtimeout "${per_timeout}" bash -c "</dev/tcp/${host}/${port}" >/dev/null 2>&1
    return $?
  fi

  # Try netcat (BSD/GNU variants)
  if command -v nc >/dev/null 2>&1; then
    # If BSD/OpenBSD nc supports -G (TCP connect timeout), use it
    if nc -h 2>&1 | grep -q -- " -G "; then
      nc -z -G "${per_timeout}" "${host}" "${port}" >/dev/null 2>&1
      return $?
    fi
    # Fallback to -w if available (GNU/OpenBSD variants). If that fails, try without a timeout.
    nc -z -w "${per_timeout}" "${host}" "${port}" >/dev/null 2>&1 || nc -z "${host}" "${port}" >/dev/null 2>&1
    return $?
  fi

  # Portable Perl fallback with explicit timeout
  if command -v perl >/dev/null 2>&1; then
    perl -MIO::Socket::INET -e '
      $h = $ENV{H}; $p = $ENV{P}; $t = $ENV{T};
      $SIG{ALRM} = sub { exit 1 };
      alarm($t);
      my $s = IO::Socket::INET->new(PeerAddr=>$h, PeerPort=>$p, Proto=>"tcp");
      exit($s ? 0 : 1);
    ' >/dev/null 2>&1 H="$host" P="$port" T="$per_timeout"
    return $?
  fi

  # Last resort: /dev/tcp without per-attempt timeout (may block longer on failure)
  bash -c "</dev/tcp/${host}/${port}" >/dev/null 2>&1
  return $?
}

start_time=$(date +%s)
while true; do
  if check_conn "${HOST}" "${PORT}" "${PER_ATTEMPT_TIMEOUT}"; then
    echo "MySQL is reachable at ${HOST}:${PORT}."
    exit 0
  fi

  now=$(date +%s)
  elapsed=$((now - start_time))
  if [[ ${elapsed} -ge ${TIMEOUT} ]]; then
    echo "Timed out after ${TIMEOUT}s waiting for MySQL at ${HOST}:${PORT}." >&2
    exit 1
  fi
  sleep "${CHECK_INTERVAL}"
done
