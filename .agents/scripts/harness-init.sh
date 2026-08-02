#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

requested_tier=""
interactive=0
case "${1:-}" in
  --interactive|-i)
    interactive=1
    ;;
  --tier)
    requested_tier="${2:-}"
    case "$requested_tier" in
      essential|standard|full) ;;
      *)
        echo "Usage: $0 [--interactive|-i] | [--tier essential|standard|full]" >&2
        exit 2
        ;;
    esac
    ;;
  "")
    ;;
  *)
    echo "Usage: $0 [--interactive|-i] | [--tier essential|standard|full]" >&2
    exit 2
    ;;
esac

mkdir -p .agents/harness/tiers .agents/scripts

required_files=(
  AGENTS.md
  .memory/context.md
  .memory/decisions.md
  .agents/harness/config.json
  .agents/skills/SKILLS_INDEX.md
  .agents/skills/app001-delivery-workflow/SKILL.md
  .agents/skills/kmp-development/SKILL.md
  .agents/skills/project-context/SKILL.md
)

missing=0
for file in "${required_files[@]}"; do
  if [ -f "$file" ]; then
    printf 'OK   %s\n' "$file"
  else
    printf 'MISS %s\n' "$file"
    missing=1
  fi
done

if [ "$missing" -ne 0 ]; then
  echo "Harness initialization found missing required files; no existing file was overwritten." >&2
  exit 1
fi

if [ "$interactive" -eq 1 ]; then
  if [ ! -t 0 ]; then
    echo "Interactive tier selection requires a terminal. Use --tier in scripts/CI." >&2
    exit 2
  fi
  echo
  echo "Select App001HeartRate harness tier:"
  echo "  1) Essential (~5 min)"
  echo "  2) Standard  (~15 min, recommended)"
  echo "  3) Full      (~30 min)"
  printf "Choice [2]: "
  read -r choice
  case "${choice:-2}" in
    1) requested_tier="essential" ;;
    2) requested_tier="standard" ;;
    3) requested_tier="full" ;;
    *) echo "Invalid choice. Select 1, 2, or 3." >&2; exit 2 ;;
  esac
fi

if [ -n "$requested_tier" ]; then
  ruby -rjson -e 'path = ".agents/harness/config.json"; data = JSON.parse(File.read(path)); data["defaultTier"] = ARGV.fetch(0); File.write(path, JSON.pretty_generate(data) + "\n")' "$requested_tier"
  echo "Harness tier changed to: $requested_tier"
else
  echo "Harness baseline is ready. Current tier is unchanged."
fi
echo "Next: run ./.agents/scripts/harness-review.sh"
