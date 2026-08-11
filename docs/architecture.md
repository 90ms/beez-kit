# Architecture

## Product goal

BeezKit gives a host Android application a one-line happy path while keeping lifecycle, concurrency, memory, accessibility, and release safety inside the library. Defaults must be useful; advanced configuration must remain optional.

## Coordinates

- Brand and root project: `BeezKit` / `beez-kit`
- Maven group: `io.github.beez`
- Kotlin namespace root: `io.github.beez.beezkit`
- Public types: `BeezKit<Type>`
- Public Modifier extensions: `Modifier.bk<Behavior>()`

The Maven group follows the GitHub-backed namespace that Maven Central can verify. Kotlin namespaces include `beezkit` to avoid collisions inside consuming applications.

## Module boundaries

- Keep unrelated third-party dependencies in adapter modules.
- Keep Inspector collectors separate and consume them with `debugImplementation` by default.
- Do not publish the sample catalog.
- Avoid umbrella artifacts until individual modules and dependency choices are stable.
- Use `implementation` unless a dependency type is part of the public API.

## Host integration

UI overlays that require a root owner may use one explicit `BeezKitHost` installation. After that installation, feature calls should be one line. Never hide Activity ownership in a process-wide singleton.

## Safety invariants

- Never retain Activity, View, Window, or composable lambdas in application-scoped objects.
- Tie coroutine work to an explicit owner and cancel it when that owner detaches.
- Bound queues, histories, payload sizes, and caches.
- Make concurrent calls deterministic and test them.
- Redact credentials and personal data in diagnostic collectors by default.
- Avoid reflection and broad R8 keep rules.
- Support accessibility semantics and reduced-motion behavior for visual components.

## Documentation contract

Every published module has one canonical file under `docs/modules`. A public API or behavior change must update its specification and README status/link in the same change.

