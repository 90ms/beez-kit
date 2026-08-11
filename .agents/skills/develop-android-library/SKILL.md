---
name: develop-android-library
description: Implement, extend, or refactor an existing BeezKit Android utility, diagnostic, or Compose component module. Use for public API work, behavior changes, tests, catalog examples, lifecycle or memory fixes, and module documentation updates. Do not use to create a brand-new Gradle module; use add-library-module instead.
---

# Develop an Android library

1. Read repository `AGENTS.md`, `docs/architecture.md`, and the affected `docs/modules/<name>.md` completely.
2. Inspect the module, its consumers, current tests, and catalog examples. Do not infer implemented behavior from a Planned target API.
3. State the smallest public happy-path API first. Keep public types prefixed `BeezKit` and Modifier extensions prefixed `bk`.
4. Before editing, check lifecycle ownership, concurrency, bounded memory, accessibility, third-party API leakage, and release/debug boundaries. Read the relevant reference below when the change touches that area.
5. Implement the smallest cohesive change. Keep optional integrations in adapters and dependencies as `implementation` unless exposed deliberately.
6. Add focused unit tests. For visual behavior, add or update a catalog example and UI tests when practical.
7. Update the canonical module spec and README status or inventory if observable behavior changed.
8. Run affected tests, lint, catalog compilation, and then the repository build when the SDK is available. Report environment-limited verification precisely.

## References

- Read [architecture](references/architecture.md) for public surface and module-boundary decisions.
- Read [Compose API guidelines](references/compose-api-guidelines.md) for Modifier, state, overlay, or accessibility work.
- Read [safety guidelines](references/safety-guidelines.md) for lifecycle, concurrency, diagnostics, or retained-state work.

