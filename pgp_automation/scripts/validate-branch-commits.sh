#!/usr/bin/env bash
# Validates that all commits on the current branch (not in the target branch) follow
# the commit message format. Used in CI to enforce conventions when local hooks are skipped.
#
# Usage: scripts/validate-branch-commits.sh [target-branch]
# Default target: origin/release_qa (falls back to origin/master or origin/main)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
TARGET="${1:-origin/release_qa}"

cd "$ROOT_DIR"

resolve_target() {
  if git rev-parse "$TARGET" &>/dev/null; then
    echo "$TARGET"
    return 0
  fi
  for fallback in origin/release_qa origin/master origin/main; do
    if git rev-parse "$fallback" &>/dev/null; then
      echo "[validate-branch-commits] Target $TARGET not found; using $fallback" >&2
      echo "$fallback"
      return 0
    fi
  done
  return 1
}

if ! RESOLVED_TARGET=$(resolve_target); then
  echo "Could not resolve target branch ($TARGET, origin/release_qa, origin/master, origin/main)."
  echo "Fetch the default branch before validating, e.g.: git fetch origin release_qa"
  exit 1
fi

COMMITS=$(git rev-list "$RESOLVED_TARGET"..HEAD 2>/dev/null || true)
if [ -z "$COMMITS" ]; then
  echo "No commits to validate (branch is up to date with $RESOLVED_TARGET or empty range)."
  exit 0
fi

FAILED=0
while read -r rev; do
  [ -n "$rev" ] || continue
  MSG=$(git log -1 --format=%B "$rev")
  FIRST_LINE=$(echo "$MSG" | head -n1)

  if echo "$FIRST_LINE" | grep -qE '^Merge(d)? (in |branch |pull request )'; then
    continue
  fi

  TMP=$(mktemp)
  echo "$FIRST_LINE" > "$TMP"
  if ! bash "$SCRIPT_DIR/validate-commit-msg.sh" "$TMP"; then
    echo "Commit $rev violates format: $FIRST_LINE"
    FAILED=1
  fi
  rm -f "$TMP"
done <<< "$COMMITS"

if [ "$FAILED" -ne 0 ]; then
  echo ""
  echo "One or more commits do not follow: <type>: <what changed>"
  echo "Types: feature, bug, fix, tests, refactor, docs, perf, chore"
  exit 1
fi

echo "All branch commits follow the required format (vs $RESOLVED_TARGET)."
