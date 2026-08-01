---
name: flow-to-design
description: Turn App001HeartRate feature requests, user journeys, and navigation changes into version-controlled design files. Use when creating or updating screen flows, UI specifications, interaction states, acceptance criteria, or implementation mappings stored in docs/design.
---

# Flow to Design

Keep the design source of truth in `docs/design/`; do not require a visual editor.

## Workflow

1. Read `docs/design/README.md` and the relevant feature folder.
2. Update the Mermaid flow first when a journey or navigation changes.
3. Define every changed screen with the template in `docs/design/templates/screen-spec.md`.
4. Map each UI action to the target `Screen` in `App.kt`; record state/data passed between screens.
5. Add acceptance criteria that can be verified on Android and iOS.
6. Keep the implementation and design files in the same change whenever possible.

## File conventions

- Store cross-feature navigation in `docs/design/flows/` as `.mmd`.
- Store a feature's specification in `docs/design/features/<feature-name>/`.
- Use `kebab-case` names. Put one screen specification in one Markdown file.
- Treat Mermaid as source code: valid syntax, short labels, no screenshots embedded as the only specification.

## Design completeness

For each screen, specify purpose, entry/exit, visible states (loading, empty, error, success), interactions, data, accessibility, and the code mapping. Do not invent navigation routes: inspect `App.kt` first.

Read [flow-format.md](references/flow-format.md) before creating a new feature flow.
