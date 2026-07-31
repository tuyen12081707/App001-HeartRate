#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
git_hooks_dir="$repo_root/.git/hooks"
source_hook="$repo_root/.githooks/prepare-commit-msg"
target_hook="$git_hooks_dir/prepare-commit-msg"

[ -d "$git_hooks_dir" ] || { echo "Không tìm thấy .git/hooks" >&2; exit 1; }

if [ -e "$target_hook" ] && [ ! -L "$target_hook" ]; then
  echo "Hook đã tồn tại: $target_hook"
  echo "Không ghi đè. Hãy backup hoặc hợp nhất thủ công nếu cần."
  exit 1
fi

ln -sf "$source_hook" "$target_hook"
chmod +x "$source_hook" "$target_hook"
echo "Đã cài memory hook: $target_hook"

