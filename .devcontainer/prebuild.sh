#!/usr/bin/env bash
# Runs on the HOST before the container is built (initializeCommand).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HOME_DIR="$SCRIPT_DIR/home"
TMP_DIR="$SCRIPT_DIR/tmp"

mkdir -p "$HOME_DIR/.claude" "$HOME_DIR/.gradle"
[ -f "$HOME_DIR/.claude.json" ] || echo '{}' > "$HOME_DIR/.claude.json"

# Bound to /tmp in the container. Never delete it: on a window reload the live
# mount would be left pointing at a deleted inode. Clear contents in place.
mkdir -p "$TMP_DIR"

# Best effort only; a failure here would abort the connect.
# After --userns=keep-id the dir is owned by a subuid, so chmod needs unshare.
ensure_mode() {
    [ "$(stat -c %a "$1")" = "1777" ] || chmod 1777 "$1"
}

ensure_mode "$TMP_DIR" 2>/dev/null \
    || podman unshare bash -c "$(declare -f ensure_mode); ensure_mode \"\$1\"" _ "$TMP_DIR" 2>/dev/null \
    || true

# Keep the socket/X11 entries: still mounted on a reload. The rest is stale.
clear_stale() {
    find "$1" -mindepth 1 -maxdepth 1 \
        ! -name 'wayland-*' \
        ! -name '.X11-unix' \
        ! -name 'pulse' \
        ! -name '.Xauthority' \
        ! -name 'vscode-wayland-*.sock' \
        -exec rm -rf {} + 2>/dev/null
}

if [ -n "$(ls -A "$TMP_DIR" 2>/dev/null)" ]; then
    clear_stale "$TMP_DIR" \
        || podman unshare bash -c "$(declare -f clear_stale); clear_stale \"\$1\"" _ "$TMP_DIR" 2>/dev/null \
        || true
fi

exit 0
