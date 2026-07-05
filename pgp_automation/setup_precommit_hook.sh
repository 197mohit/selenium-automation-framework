#!/bin/bash
# Deprecated: use scripts/install-hooks.sh instead.
# Kept for backward compatibility with existing docs and workflows.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec bash "$SCRIPT_DIR/scripts/install-hooks.sh"
