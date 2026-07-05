#!/usr/bin/env bash
# Installs git hooks for commit message validation and branch naming enforcement.
#
# Strategy:
#   1. If `pre-commit` CLI is available, use it (manages .git/hooks/ automatically,
#      covers all hooks in .pre-commit-config.yaml)
#   2. Otherwise, copy standalone hooks from .githooks/ into .git/hooks/
#      (covers commit-msg, pre-push, and pre-commit at minimum)
#
# Safe to run multiple times — existing hooks are backed up before overwriting.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

if [ ! -d "$ROOT_DIR/.git" ]; then
  error "Not in a git repository. Run from the repo root."
fi

install_with_precommit() {
  info "Installing hooks via pre-commit..."
  (cd "$ROOT_DIR" && pre-commit install && pre-commit install --hook-type commit-msg && pre-commit install --hook-type pre-push)
  info "pre-commit hooks installed (pre-commit, commit-msg, pre-push)."
}

backup_existing_hook() {
  local hook_path="$1"
  if [ -f "$hook_path" ] && [ ! -L "$hook_path" ]; then
    local backup="${hook_path}.backup.$(date +%s)"
    warn "Backing up existing hook to $(basename "$backup")"
    mv "$hook_path" "$backup"
  elif [ -L "$hook_path" ]; then
    rm -f "$hook_path"
  fi
}

install_standalone() {
  info "pre-commit not found — installing standalone hooks from .githooks/..."
  local hooks_dir="$ROOT_DIR/.git/hooks"
  mkdir -p "$hooks_dir"

  for hook in "$ROOT_DIR/.githooks/"*; do
    [ -f "$hook" ] || continue
    local hook_name
    hook_name=$(basename "$hook")
    local target="$hooks_dir/$hook_name"

    backup_existing_hook "$target"

    cp "$hook" "$target"
    chmod +x "$target"
    info "Installed $hook_name hook."
  done

  info "Standalone hooks installed. For the full hook suite (YAML checks, secret detection),"
  info "install pre-commit: pip install pre-commit && bash $0"
}

try_install_precommit() {
  info "Attempting to install pre-commit..."
  if command -v pipx >/dev/null 2>&1; then
    pipx install pre-commit && return 0
  elif command -v pip3 >/dev/null 2>&1; then
    pip3 install --user pre-commit && return 0
  elif command -v pip >/dev/null 2>&1; then
    pip install --user pre-commit && return 0
  elif command -v brew >/dev/null 2>&1; then
    brew install pre-commit && return 0
  fi
  return 1
}

if command -v pre-commit >/dev/null 2>&1; then
  install_with_precommit
elif try_install_precommit 2>/dev/null && command -v pre-commit >/dev/null 2>&1; then
  info "pre-commit installed successfully."
  install_with_precommit
else
  warn "Could not auto-install pre-commit. Install manually: pip install pre-commit"
  install_standalone
fi

info "Hook installation complete."
info ""
info "Commit messages must follow: <type>: <what changed>"
info "Types: feature, bug, fix, tests, refactor, docs, perf, chore"
info "Branch names must follow: <prefix>/<description> (see .foundry/casts/collaboration.md)"
info "Integration branches allowed: master, main, release_qa"
info "Ticket branches allowed: PGP-<id>, PGQA-<id>"
