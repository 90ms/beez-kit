# BeezKit

[한국어](README.md) | [English](README.en.md)

BeezKit is a Compose-first Android toolkit for adding small utilities, diagnostics, and reusable UI components to a host app with minimal setup. Public API types use the `BeezKit` prefix, while Modifier extensions use the compact `bk` prefix.

> Status: early development. Throttle, Stack Trace, Measure, and Skeleton are available experimentally; the other modules are scaffolded only.

## Libraries

| Category | Library | Artifact | Status | Specification |
| --- | --- | --- | --- | --- |
| Toolkit | Throttle | `io.github.beez:throttle` | Experimental | [English](docs/modules/throttle.en.md) · [한국어](docs/modules/throttle.md) |
| Toolkit | Stack trace | `io.github.beez:stacktrace` | Experimental | [English](docs/modules/stacktrace.en.md) · [한국어](docs/modules/stacktrace.md) |
| Toolkit | Measure | `io.github.beez:measure` | Experimental | [English](docs/modules/measure.en.md) · [한국어](docs/modules/measure.md) |
| Debug | Inspector Core | `io.github.beez:inspector-core` | Planned | [Details](docs/modules/inspector.md) |
| Debug | Inspector Network | `io.github.beez:inspector-network` | Planned | [Details](docs/modules/inspector.md#network-collector) |
| Debug | Inspector Event | `io.github.beez:inspector-event` | Planned | [Details](docs/modules/inspector.md#event-collector) |
| Debug | Inspector WebView | `io.github.beez:inspector-webview` | Planned | [Details](docs/modules/inspector.md#webview-collector) |
| Component | Toast | `io.github.beez:toast` | Planned | [Details](docs/modules/toast.md) |
| Component | Snackbar | `io.github.beez:snackbar` | Planned | [Details](docs/modules/snackbar.md) |
| Component | Tooltip | `io.github.beez:tooltip` | Planned | [Details](docs/modules/tooltip.md) |
| Component | Skeleton | `io.github.beez:skeleton` | Experimental | [English](docs/modules/skeleton.en.md) · [한국어](docs/modules/skeleton.md) |

## Main APIs

BeezKit aims for a one-line default path with optional configuration only when needed.

```kotlin
BeezKitToast.success("Saved")
BeezKitSnackbar.error("Network error")
BeezKitMeasure.trace("load-user") { repository.loadUser() }
BeezKitStackTrace.log(userId)

Modifier.bkThrottledClickable { submit() }
Modifier.bkSkeleton(visible = isLoading)
Modifier.bkTooltip("Edit your profile here")
```

The Throttle, Stack Trace, Measure, and Skeleton snippets are available now. The remaining snippets describe target APIs and do not compile until their modules are implemented.

Detailed documentation for implemented modules is Korean-first, with an equivalent English specification linked from each document header and the library table.

## Getting started with Skeleton

```kotlin
dependencies {
    implementation("io.github.beez:skeleton:<version>")
}
```

Use `Modifier.bkSkeleton` for one component, `BeezKitSkeletonScope` for a region whose placeholders should animate together, and `BeezKitSkeletonContainer` to switch between a dedicated loading layout and real screen content.

See the [English specification](docs/modules/skeleton.en.md) for state ownership, animation, styling, minimum visibility, accessibility, and lifecycle behavior. A [Korean specification](docs/modules/skeleton.md) is also available.

## Project layout

```text
toolkit/       Small runtime and diagnostic libraries
components/    Compose-first UI components
samples/       Host-app integration and component catalog
build-logic/   Shared Gradle convention plugins
docs/modules/  Canonical module specifications
.agents/skills Repository-local Codex workflows
```

See [architecture](docs/architecture.md) for module boundaries and design rules, and the [catalog specification](docs/sample-catalog.md) for sample registration and screen requirements.

## Build

Install Android SDK 36, use JDK 17 or newer, and run:

```shell
./gradlew build
```

The catalog application is available at `:samples:catalog`.
