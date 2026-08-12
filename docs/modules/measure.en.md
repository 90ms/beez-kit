# Measure

[한국어](measure.md) | [English](measure.en.md)

**Module:** `:toolkit:measure`
**Artifact:** `io.github.beez:measure`
**Status:** Experimental

## Purpose

Measure records elapsed time for a code block or between separate start and end points. Measurement never changes the host return value, exception, cancellation, or control flow.

## Installation

```kotlin
dependencies {
    implementation("io.github.beez:measure:<version>")
}
```

Use `debugImplementation` when diagnostics are needed only in development builds. When shipping the module in release builds, configure `enabled` and reporters explicitly.

## Basic API

```kotlin
val user = BeezKitMeasure.trace("load-user") {
    repository.loadUser()
}

val user = BeezKitMeasure.traceSuspend("load-user") {
    repository.loadUser()
}
```

- The block's value is returned unchanged.
- A normal exception is recorded as `Failure`; coroutine cancellation is recorded as `Cancelled`.
- The same exception or cancellation is rethrown after measurement.
- Disabled mode executes only the block, without input validation or time measurement.

## Separate spans

```kotlin
val span = BeezKitMeasure.start("app-start")
initializeApplication()
span.end()
```

- Every active span has a process-unique ID.
- Spans with the same tag may run concurrently.
- Only the first `end()` returns a result; later calls return `null`.
- `BeezKitMeasureSpan` implements `AutoCloseable` and can be used with `use`.

Tag-based convenience calls pair identical tag and key values in LIFO order.

```kotlin
BeezKitMeasure.markStart("load-item", key = itemId)
BeezKitMeasure.markEnd("load-item", key = itemId)
```

- `markEnd()` returns `null` without a matching start.
- Prefer independent spans for concurrent work.
- Active tag spans are bounded by `maxActiveMarks`; `markStart()` returns `false` at capacity.

## Result model

```kotlin
data class BeezKitMeasureRecord(
    val id: String,
    val tag: String,
    val duration: Duration,
    val status: BeezKitMeasureStatus,
    val attributes: Map<String, String>,
    val error: Throwable?,
)
```

Statuses are `Success`, `Failure`, and `Cancelled`. Attributes are copied into an immutable snapshot when measurement begins. History is disabled by default because retained records may hold an error object.

## Attribute limits

- At most 16 attributes
- Tag length at most 128 characters and not blank
- Key length at most 64 characters and not blank
- Value length at most 256 characters
- Violations throw `IllegalArgumentException`
- Never include passwords, tokens, or personal information

## Configuration

```kotlin
BeezKitMeasure.configure {
    enabled = BuildConfig.DEBUG
    historyCapacity = 100
    maxActiveMarks = 1_024
    reporter(appReporter)
}
```

| Option | Default |
| --- | --- |
| `enabled` | `true` |
| `historyCapacity` | `0` |
| `maxActiveMarks` | `1,024` |
| reporter | none |

- Configuration changes apply to measurements started afterward.
- Reconfiguration clears history and unfinished tag-based spans.
- A directly created `BeezKitMeasureSpan` completes with the configuration captured at start.
- Do not register an Activity, View, or composable callback as a global reporter.

## Reporters and history

```kotlin
fun interface BeezKitMeasureReporter {
    fun report(record: BeezKitMeasureRecord)
}

val records = BeezKitMeasure.records()
BeezKitMeasure.clear()
```

- Reporters run synchronously on the thread that completes measurement.
- Reporting creates no coroutine or worker thread.
- An exception from one reporter does not affect host code or other reporters.
- History is a fixed-capacity ring buffer that evicts the oldest record.
- `records()` returns an immutable snapshot ordered from oldest to newest.

## Time and concurrency

- Elapsed time uses the monotonic `SystemClock.elapsedRealtimeNanos()` clock.
- A negative elapsed value is clamped to zero.
- Every public API is safe to call from multiple threads.
- Span completion, history, configuration, and tag stacks are synchronized.
- Completed tag spans are removed from the active stack immediately.

## Catalog and verification

The catalog demonstrates return-value preservation, failure rethrowing, independent spans, nested identical tags, and the ten most recent history records using the real API.

Tests cover return values, success, failure, cancellation, disabled mode, history eviction, tag LIFO behavior, active limits, unique IDs, concurrent completion, reporter isolation, and attribute limits.

## Excluded from the first release

- Sampling and slow thresholds
- Parent-child spans
- Percentiles and statistical aggregation
- Disk storage and network export
- Timber, Firebase, and OpenTelemetry adapters
- Inspector UI integration
