#!/usr/bin/env bash
# Unlocks secrets/ for a CI run. Locally, git-crypt handles this at clone time.
# CI stores the symmetric key in SECRETS_KEY and unlocks without leaving the key on disk.
set -euo pipefail

if [ -z "${SECRETS_KEY:-}" ]; then
  echo "SECRETS_KEY is not set - skipping secret unlock"
  exit 0
fi

if ! command -v git-crypt >/dev/null; then
  echo "git-crypt is not installed - install it before unlocking secrets" >&2
  exit 1
fi

tmp_key="$(mktemp)"
trap 'rm -f "$tmp_key"' EXIT
printf '%s' "$SECRETS_KEY" | base64 --decode > "$tmp_key"
git-crypt unlock "$tmp_key"
echo "secrets unlocked"
