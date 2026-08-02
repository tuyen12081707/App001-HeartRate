#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

failures=0

check_file() {
  if [ -f "$1" ]; then
    printf 'OK   %s\n' "$1"
  else
    printf 'FAIL %s\n' "$1"
    failures=$((failures + 1))
  fi
}

echo "== Harness files =="
check_file AGENTS.md
check_file .memory/context.md
check_file .memory/decisions.md
check_file .agents/harness/config.json
check_file .agents/skills/SKILLS_INDEX.md

echo "== Config =="
if ruby -rjson -e 'data = JSON.parse(File.read(".agents/harness/config.json")); abort unless data["automation"]["autoCommit"] == false && data["automation"]["autoPush"] == false && data["automation"]["overwriteExistingFiles"] == false' 2>/dev/null; then
  echo "OK   safe automation defaults"
else
  echo "FAIL safe automation defaults"
  failures=$((failures + 1))
fi

echo "== Skill frontmatter =="
for skill_file in .agents/skills/*/SKILL.md; do
  if ruby -ryaml -e 'parts = File.read(ARGV[0]).split(/^---\s*$\n?/); data = YAML.safe_load(parts[1]); abort unless data["name"] && data["description"]' "$skill_file" 2>/dev/null; then
    printf 'OK   %s\n' "$skill_file"
  else
    printf 'FAIL %s\n' "$skill_file"
    failures=$((failures + 1))
  fi
done

echo "== Stale agent references =="
if rg -n --hidden --glob '!build/**' --glob '!.git/**' '(^|[[:space:]`])agent/(memory\.sh|install-memory-hook\.sh|AUTO_WORKFLOW\.md|GIT_COMMIT_RULES\.md|KMP_AGENTS\.md)' . >/tmp/app001-harness-stale-refs.txt; then
  cat /tmp/app001-harness-stale-refs.txt
  failures=$((failures + 1))
else
  echo "OK   no stale agent/ references"
fi

echo "== Working tree =="
git status --short --untracked-files=all

if [ "$failures" -gt 0 ]; then
  echo "Harness review failed with $failures issue(s)." >&2
  exit 1
fi
echo "Harness review passed."
