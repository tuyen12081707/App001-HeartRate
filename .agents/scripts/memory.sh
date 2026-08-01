#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
memory_dir="$repo_root/.memory"

die() { echo "Lỗi: $*" >&2; exit 1; }

append_entry() {
  local file="$1"
  local label="$2"
  local value="$3"
  local today
  today="$(date +%F)"
  {
    echo
    echo "## $today — $label"
    echo
    echo "- Nội dung: $value"
    echo "- Trạng thái: active"
  } >> "$memory_dir/$file"
  echo "Đã ghi vào .memory/$file"
}

mkdir -p "$memory_dir"
command="${1:-show}"
shift || true

case "$command" in
  show)
    cat "$memory_dir/context.md" "$memory_dir/decisions.md" "$memory_dir/inbox.md"
    ;;
  search)
    [ "$#" -eq 1 ] || die 'dùng: memory.sh search "từ khóa"'
    rg -n -i --glob '*.md' -- "$1" "$memory_dir" || true
    ;;
  decision)
    [ "$#" -eq 1 ] || die 'dùng: memory.sh decision "quyết định"'
    append_entry decisions.md "Decision" "$1"
    ;;
  context)
    [ "$#" -eq 1 ] || die 'dùng: memory.sh context "bối cảnh"'
    append_entry context.md "Context" "$1"
    ;;
  inbox)
    [ "$#" -eq 1 ] || die 'dùng: memory.sh inbox "ghi chú"'
    append_entry inbox.md "Note" "$1"
    ;;
  *)
    die "lệnh không hợp lệ: $command (dùng show, search, decision, context hoặc inbox)"
    ;;
esac
