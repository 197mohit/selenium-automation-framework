#!/usr/bin/env bash
# Validates commit message format per .foundry/casts/collaboration.md:
#   <type>: <what changed>
# Types: feature, bug, fix, tests, refactor, docs, perf, chore (optional scope: type(scope): ...)
# First line must be 72 characters or fewer.
set -euo pipefail

COMMIT_MSG_FILE="${1:-}"
if [ -z "$COMMIT_MSG_FILE" ] || [ ! -f "$COMMIT_MSG_FILE" ]; then
  echo "Usage: $0 <path-to-commit-msg-file>"
  exit 1
fi

FIRST_LINE=$(head -n1 "$COMMIT_MSG_FILE")
MAX_LEN=72

FORMAT_REGEX='^(feature|bug|fix|tests|refactor|docs|perf|chore)(\([^)]+\))?: .+'

if ! echo "$FIRST_LINE" | grep -qE "$FORMAT_REGEX"; then
  echo "Invalid commit message format."
  echo "Required: <type>: <what changed>"
  echo "Types: feature, bug, fix, tests, refactor, docs, perf, chore (optional scope: type(scope): ...)"
  echo "Example: feature: add checkout revamp 2 test suite"
  echo "Got: $FIRST_LINE"
  exit 1
fi

LEN=${#FIRST_LINE}
if [ "$LEN" -gt "$MAX_LEN" ]; then
  echo "Commit message first line must be under ${MAX_LEN} characters (got $LEN)."
  echo "Got: $FIRST_LINE"
  exit 1
fi

exit 0
