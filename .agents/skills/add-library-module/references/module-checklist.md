# Module checklist

- A clear reason exists for a separate artifact.
- Gradle path and artifact ID are lowercase and concise.
- Namespace begins with `io.github.beez.beezkit` and is unique.
- Public top-level types use `BeezKit`; Modifier extensions use `bk`.
- Optional third-party integrations are adapter modules.
- Inspector or developer-only integrations are documented for `debugImplementation`.
- A canonical spec and README link exist.
- Unit tests exist once implementation begins; visual modules have a catalog destination.
- No Activity, View, Window, WebView, or callback is retained globally.
- Queues, histories, and payloads are bounded.

