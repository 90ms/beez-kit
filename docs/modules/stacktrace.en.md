# Stack Trace

[한국어](stacktrace.md) | [English](stacktrace.en.md)

**Module:** `:toolkit:stacktrace`
**Artifact:** `io.github.beez:stacktrace`
**Status:** Experimental

## Purpose

Stack Trace captures the call path that reached the current code together with a bounded value. It is intended for diagnosing duplicate calls, unexpected state changes, navigation, and lifecycle paths. It does not replace normal logging or exception handling.

## Installation

```kotlin
dependencies {
    implementation("io.github.beez:stacktrace:<version>")
}
```

Use `debugImplementation` when the feature should exist only in diagnostic builds.

```kotlin
debugImplementation("io.github.beez:stacktrace:<version>")
```

## Basic API

```kotlin
BeezKitStackTrace.log(userId)

BeezKitStackTrace.log(
    value = userId,
    tag = "user-update",
)
```

- A value is converted to a string at the call site.
- The original object and `StackTraceElement` instances are not retained in records or history.
- The returned `BeezKitStackTraceRecord?` is available for immediate inspection and is `null` while disabled.

Use the lazy API for an expensive value.

```kotlin
BeezKitStackTrace.log(tag = "state-change") {
    "state=${state::class.simpleName}"
}
```

- The lambda and stack collector are skipped while disabled.
- An exception from the lambda is isolated as `<value-provider-error: Type>`.
- Exception messages are not recorded because they may be sensitive or large.

## Per-call options

```kotlin
BeezKitStackTrace.log(
    value = route,
    tag = "navigation",
    maxFrames = 12,
    skipFrames = 1,
    includeFrameworkFrames = false,
)
```

| Option | Meaning |
| --- | --- |
| `tag` | Optional identifier of at most 64 characters |
| `maxFrames` | Maximum frames retained for this call |
| `skipFrames` | Additional leading frames removed after filtering |
| `includeFrameworkFrames` | Whether Android, Compose, and coroutine frames are included |

`maxFrames` must be from 1 through 256 and `skipFrames` must be non-negative. Omitted options use global configuration.

## Result model

```kotlin
data class BeezKitStackTraceRecord(
    val tag: String?,
    val value: String,
    val frames: List<BeezKitStackFrame>,
)

data class BeezKitStackFrame(
    val className: String,
    val methodName: String,
    val fileName: String?,
    val lineNumber: Int?,
)
```

- Frames are an immutable snapshot.
- An unavailable file name or non-positive line number is represented by `null`.
- A record with an empty frame list is still created when filtering removes every frame.

## Configuration

```kotlin
BeezKitStackTrace.configure {
    enabled = BuildConfig.DEBUG
    historyCapacity = 50
    defaultMaxFrames = 8
    maxValueLength = 512
    includeFrameworkFrames = false

    excludePackage("com.example.common.logging")
    reporter(appReporter)
}
```

| Option | Default |
| --- | ---: |
| `enabled` | `true` |
| `historyCapacity` | `0` |
| `defaultMaxFrames` | `8` |
| `maxValueLength` | `512` |
| `includeFrameworkFrames` | `false` |
| reporter | none |

- `historyCapacity` is capped at 10,000 and `maxValueLength` at 16,384.
- Reporters and excluded package prefixes are each capped at 32.
- Configuration changes affect later calls and clear existing history.
- A zero history capacity retains no records globally.
- The module does not write to Logcat automatically.
- Do not register an Activity, View, or composable callback as a global reporter.

## Value conversion and privacy

An immediate `null` becomes `"null"`; other values use `toString()`. An exception from `toString()` becomes `<value-format-error: Type>`.

A value beyond `maxValueLength` is bounded and receives the `…[truncated]` suffix. Never pass passwords, authentication tokens, or personal information. Prefer the lazy API when a sensitive object must be reduced to a safe diagnostic string.

## Frame filtering

Filtering is applied in this order:

1. Remove BeezKit StackTrace and `Thread` internals.
2. Remove reflection frames.
3. Remove user-configured package prefixes.
4. Remove Android, Compose, and coroutine frames by default.
5. Apply `skipFrames`.
6. Apply `maxFrames`.
7. Convert to the immutable public model.

Default framework prefixes are `android.`, `androidx.compose.`, `kotlin.coroutines.`, and `kotlinx.coroutines.`. Reflection frames are always removed. Set `includeFrameworkFrames = true` only for calls that need them.

User filters are package prefixes rather than regular expressions. At most 32 may be registered; each must be non-blank and no longer than 200 characters.

## Reporters and history

```kotlin
fun interface BeezKitStackTraceReporter {
    fun report(record: BeezKitStackTraceRecord)
}

val records = BeezKitStackTrace.records()
BeezKitStackTrace.clear()
```

- Reporters run synchronously on the calling thread.
- No coroutine or worker is created for reporting.
- An exception from one reporter does not affect the host or other reporters.
- History is a fixed-capacity ring buffer that evicts the oldest record.
- `records()` returns an immutable snapshot ordered from oldest to newest.

## Disabled mode and safety

Disabled mode skips value conversion, the lazy lambda, stack collection, filtering, record creation, reporting, and history storage.

All public APIs may be called from multiple threads. Configuration and history access are synchronized, and active calls are not retained in a global collection. Value length, frame count, filter count, and history capacity are always bounded.

## Catalog and tests

The catalog demonstrates basic recording, a lazy wrapper with `skipFrames`, framework inclusion, bounded history, and the disabled fast path using the real API.

Tests cover internal and framework filtering, package exclusion, skip and limit behavior, disabled mode, value-format failures, lazy failures, string bounds, reporter isolation, history eviction, immutable snapshots, and concurrent recording.

## Excluded from the first release

- Per-tag rate limiting and sampling
- Merging identical stacks and counting occurrences
- Disk or network storage
- Timber, Firebase, and OpenTelemetry adapters
- Inspector UI integration
- R8/ProGuard mapping and source links
