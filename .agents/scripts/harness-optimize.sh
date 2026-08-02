#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

warnings=0
blockers=0

echo "== Oversized skills (>500 lines) =="
while IFS= read -r skill_file; do
  lines=$(wc -l < "$skill_file" | tr -d ' ')
  if [ "$lines" -gt 500 ]; then
    printf 'WARN %s (%s lines)\n' "$skill_file" "$lines"
    warnings=$((warnings + 1))
  fi
done < <(find .agents/skills -name SKILL.md -type f | sort)

echo "== Duplicate skill names =="
names_file=$(mktemp)
trap 'rm -f "$names_file"' EXIT
for skill_file in .agents/skills/*/SKILL.md; do
  ruby -ryaml -e 'p YAML.safe_load(File.read(ARGV[0]).split(/^---\s*$\n?/)[1])["name"]' "$skill_file" 2>/dev/null || true
done | sort > "$names_file"
if [ "$(sort "$names_file" | uniq -d | wc -l | tr -d ' ')" -gt 0 ]; then
  sort "$names_file" | uniq -d | sed 's/^/WARN duplicate: /'
  blockers=$((blockers + 1))
else
  echo "OK   no duplicate skill names"
fi

echo "== Index coverage reminder =="
echo "Review .agents/skills/SKILLS_INDEX.md whenever a SKILL.md is added or renamed."

if [ "$blockers" -gt 0 ]; then
  echo "Optimization found $blockers blocker(s) and $warnings warning(s)."
  exit 1
fi
echo "Optimization completed with $warnings warning(s) and no blockers."
