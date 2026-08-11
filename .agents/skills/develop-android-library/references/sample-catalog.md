# Sample catalog guidance

- Treat repository `docs/sample-catalog.md` as the canonical catalog contract and read it completely.
- Register every public Gradle module in `CatalogRegistry.kt`, even when multiple Inspector modules share one future Playground.
- Keep entries discoverable by a stable kebab-case id and exact Gradle module path.
- Leave unimplemented modules as honest Planned screens; do not create fake public APIs.
- When implementation begins, replace Planned content with a sample that directly invokes the real public API.
- Demonstrate the one-line happy path, meaningful options, observable results, and relevant edge states.
- Keep sample-only UI, state, fake data, and Material 3 types inside `samples:catalog`.
- Do not add Beez Design until a public artifact is available and the user explicitly requests the migration.
- Keep README and catalog maturity states aligned in the same change.
