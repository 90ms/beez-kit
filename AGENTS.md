# BeezKit repository instructions

## Product contract

- Build Compose-first Android libraries with a one-line happy path and optional advanced configuration.
- Prefix public top-level types with `BeezKit`; prefix public Modifier extensions with `bk`.
- Use Maven group `io.github.beez` and namespace root `io.github.beez.beezkit`.
- Treat `docs/modules/*.md` as the canonical behavior specifications.

## Architecture and safety

- Keep modules cohesive and split adapters when they introduce optional third-party dependencies.
- Use `implementation` unless a dependency type is intentionally exposed by the public API.
- Do not retain Activity, View, Window, WebView, or composable callbacks in application-scoped state.
- Give coroutine work an explicit lifecycle owner and cancel it on detach.
- Bound all queues, histories, buffers, payloads, and caches.
- Make diagnostic features safe for sensitive data and prefer `debugImplementation` for Inspector modules.
- Prefer `Modifier.Node` for stateful performance-sensitive Modifier behavior.
- Avoid reflection and broad consumer R8 keep rules.

## Change workflow

- Read `docs/architecture.md` and the affected module specification before implementation.
- Update the module specification and root README when public API, behavior, status, or artifact inventory changes.
- Add unit tests for behavior and host integration examples to `samples/catalog` when UI becomes visible.
- Run the narrowest affected tests first, then `./gradlew build` when an Android SDK is available.
- Do not mark a README module `Stable` without API, safety, test, documentation, and release verification.

## Repository skills

- Use `$develop-android-library` to implement or extend an existing module.
- Use `$add-library-module` to add a Gradle module and its complete documentation wiring.
- Use `$verify-android-library` for readiness, safety, API, and build verification.

