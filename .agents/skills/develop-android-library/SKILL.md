---
name: develop-android-library
description: Implement, extend, or refactor an existing BeezKit Android utility, diagnostic, or Compose component module. Use for public API work, behavior changes, tests, catalog examples, lifecycle or memory fixes, and module documentation updates. Do not use to create a brand-new Gradle module; use add-library-module instead.
---

# Develop an Android library

1. Read repository `AGENTS.md`, `docs/architecture.md`, `docs/sample-catalog.md`, and the affected `docs/modules/<name>.md` completely.
2. Inspect the module, its consumers, current tests, and catalog examples. Do not infer implemented behavior from a Planned target API.
3. State the smallest public happy-path API first. Keep public types prefixed `BeezKit` and Modifier extensions prefixed `bk`.
4. Before editing, check lifecycle ownership, concurrency, bounded memory, accessibility, third-party API leakage, and release/debug boundaries. Read the relevant reference below when the change touches that area.
5. Implement the smallest cohesive change. Keep optional integrations in adapters and dependencies as `implementation` unless exposed deliberately.
6. Add focused unit tests. Add or update a discoverable catalog entry for every public module, including non-visual utilities. The sample must call the real public API and expose useful results or state once implementation exists.
7. Update the canonical module spec, catalog status, and README status or inventory in the same change when observable behavior changes.
8. Run local static checks only. Do not run Gradle locally. When push or pull-request authority exists, use the Android validation workflow for tests, lint, and catalog assembly, then report its result.

## References

- Read [architecture](references/architecture.md) for public surface and module-boundary decisions.
- Read [Compose API guidelines](references/compose-api-guidelines.md) for Modifier, state, overlay, or accessibility work.
- Read [safety guidelines](references/safety-guidelines.md) for lifecycle, concurrency, diagnostics, or retained-state work.
- Read [sample catalog guidance](references/sample-catalog.md) before changing a public module or catalog entry.
