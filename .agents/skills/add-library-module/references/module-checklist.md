# Module checklist

- A clear reason exists for a separate artifact.
- Gradle path and artifact ID are lowercase and concise.
- Namespace begins with `io.github.beez.beezkit` and is unique.
- Public top-level types use `BeezKit`; Modifier extensions use `bk`.
- Optional third-party integrations are adapter modules.
- Inspector or developer-only integrations are documented for `debugImplementation`.
- A canonical spec and README link exist.
- A catalog entry contains the exact Gradle path and the catalog app depends on the module.
- Unit tests and a real public-API sample replace Planned content once implementation begins.
- No Activity, View, Window, WebView, or callback is retained globally.
- Queues, histories, and payloads are bounded.
- README and catalog maturity states agree.
