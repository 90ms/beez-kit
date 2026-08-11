# Verification checklist

## API

- One-line default path; optional advanced configuration.
- `BeezKit` public type prefix and `bk` Modifier prefix.
- No accidental third-party public types.
- Clear no-host, disabled, failure, and cancellation behavior.

## Runtime safety

- No global Activity/View/Window/WebView/callback retention.
- Owned and cancelled coroutines, listeners, timers, and animations.
- Deterministic concurrent behavior.
- Bounded queues, history, payloads, caches, and formatting.
- Secret redaction before storage or display.

## Compose and UI

- Recomposition and parameter updates preserve correct state.
- Detach/reattach and configuration changes are safe.
- Semantics, TalkBack, action timing, and reduced motion are covered.
- Large-list behavior avoids per-item unbounded work.

## Packaging

- Minimal dependencies and correct `api`/`implementation` usage.
- Debug tooling stays out of release dependency graphs.
- No unnecessary resources, manifest components, reflection, or broad keep rules.

## Evidence

- Unit tests cover boundaries, concurrency, cancellation, and errors.
- Catalog demonstrates visual public APIs.
- Specs and README match implementation.
- Build, lint, tests, minified consumer sample, and artifact size are checked before Stable.

