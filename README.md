# BeezKit

BeezKit is a Compose-first Android toolkit for adding small utilities, diagnostics, and reusable UI components to a host app with minimal setup. Public APIs use the `BeezKit` prefix, while Modifier extensions use the compact `bk` prefix.

> Status: project foundation. The modules below are scaffolded; public APIs are not implemented yet.

## Libraries

| Category | Library | Artifact | Status | Specification |
| --- | --- | --- | --- | --- |
| Toolkit | Throttle | `io.github.beez:throttle` | Planned | [Details](docs/modules/throttle.md) |
| Toolkit | Stack trace | `io.github.beez:stacktrace` | Planned | [Details](docs/modules/stacktrace.md) |
| Toolkit | Measure | `io.github.beez:measure` | Planned | [Details](docs/modules/measure.md) |
| Debug | Inspector Core | `io.github.beez:inspector-core` | Planned | [Details](docs/modules/inspector.md) |
| Debug | Inspector Network | `io.github.beez:inspector-network` | Planned | [Details](docs/modules/inspector.md#network-collector) |
| Debug | Inspector Event | `io.github.beez:inspector-event` | Planned | [Details](docs/modules/inspector.md#event-collector) |
| Debug | Inspector WebView | `io.github.beez:inspector-webview` | Planned | [Details](docs/modules/inspector.md#webview-collector) |
| Component | Toast | `io.github.beez:toast` | Planned | [Details](docs/modules/toast.md) |
| Component | Snackbar | `io.github.beez:snackbar` | Planned | [Details](docs/modules/snackbar.md) |
| Component | Tooltip | `io.github.beez:tooltip` | Planned | [Details](docs/modules/tooltip.md) |
| Component | Skeleton | `io.github.beez:skeleton` | Planned | [Details](docs/modules/skeleton.md) |

## Intended API

```kotlin
BeezKitToast.success("Saved")
BeezKitSnackbar.error("Network error")
BeezKitMeasure.trace("load-user") { repository.loadUser() }
BeezKitStackTrace.log(userId)

Modifier.bkThrottledClickable { submit() }
Modifier.bkSkeleton(visible = isLoading)
Modifier.bkTooltip("Edit your profile here")
```

These snippets describe the target API and do not compile until their modules are implemented.

## Project layout

```text
toolkit/       Small runtime and diagnostic libraries
components/    Compose-first UI components
samples/       Host-app integration and component catalog
build-logic/   Shared Gradle convention plugins
docs/modules/  Canonical module specifications
.agents/skills Repository-local Codex workflows
```

See [architecture](docs/architecture.md) for module boundaries and design rules.

## Build

Install Android SDK 37, use JDK 17 or newer, and run:

```shell
./gradlew build
```

The catalog application is available at `:samples:catalog`.

