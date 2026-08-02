#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

kind="${1:-}"
message="${2:-}"

case "$kind" in
  context|decision|inbox) ;;
  *)
    echo "Usage: $0 <context|decision|inbox> \"confirmed note\"" >&2
    exit 2
    ;;
esac

[ -n "$message" ] || { echo "A non-empty confirmed note is required." >&2; exit 2; }

./.agents/scripts/memory.sh "$kind" "$message"
echo "Review these SSOT files if the note changes project behavior or architecture:"
echo "- BRAINSTORM.md"
echo "- .agents/skills/project-context/SKILL.md"
echo "- .agents/skills/SKILLS_INDEX.md (when a skill changes)"
