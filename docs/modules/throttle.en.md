# Throttle

[한국어](throttle.md) | [English](throttle.en.md)

**Module:** `:toolkit:throttle`
**Artifact:** `io.github.beez:throttle`
**Status:** Experimental

## Purpose

Throttle limits rapidly repeated Compose clicks to prevent duplicate submissions, navigation, or payment requests. It uses leading-edge behavior: the first click runs immediately and later clicks inside the interval are ignored.

The module does not run business work or coroutines. It only controls when the host-provided `onClick` is invoked; the host owns success, failure, cancellation, and loading state.

## Installation

```kotlin
dependencies {
    implementation("io.github.beez:throttle:<version>")
}
```

## Basic API

The default interval is `500.milliseconds`.

```kotlin
Surface(
    modifier = Modifier.bkThrottledClickable {
        openDetail()
    },
) {
    ProductCardContent()
}
```

Apply the Modifier to a `Box`, `Surface`, or custom component that does not already own click behavior. For a component such as `Button` with its own `onClick`, prefer controlling that component directly to avoid duplicate click semantics.

## Options

```kotlin
Modifier.bkThrottledClickable(
    interval = 750.milliseconds,
    enabled = true,
    onClickLabel = "Submit order",
    role = Role.Button,
) {
    submitOrder()
}
```

| Option | Default | Meaning |
| --- | --- | --- |
| `interval` | `500.milliseconds` | Time during which clicks after an accepted click are suppressed |
| `enabled` | `true` | Disables pointer and accessibility click actions when false |
| `onClickLabel` | `null` | Action description exposed to accessibility services |
| `role` | `null` | Accessibility role such as Button |
| `onClick` | required | Latest callback invoked by an accepted click |

The default is also exposed as `BeezKitDefaultThrottleInterval`.

## Click rules

```text
First click                  -> accepted immediately
Later click inside interval -> ignored
Click at or after boundary  -> accepted and starts a new interval
```

- Every Modifier instance owns independent throttle state.
- Two buttons do not suppress each other.
- The last accepted time survives recomposition.
- The latest `onClick` callback is used after recomposition.
- Interval changes apply to the next click.
- A negative interval throws `IllegalArgumentException`.
- A zero interval accepts every click.

## Disabled behavior

With `enabled = false`, behavior follows Compose `clickable`, including indication, pointer input, click semantics, and accessibility state. Re-enabling the same Modifier instance preserves its throttle record.

Throttle does not represent in-progress work or automatically disable a target until work completes. The host should control `enabled` and its loading UI for long-running operations.

## Time, lifecycle, and memory

- Elapsed time uses the monotonic `SystemClock.elapsedRealtimeNanos()` clock.
- Wall-clock and time-zone changes have no effect.
- Time is read only on a click; no coroutine, timer, or worker is created.
- Each composed Modifier retains one small gate.
- No Activity, View, Context, or composable callback is stored in process-global state.
- Removing the Modifier from composition releases its gate and callback references.
- If the monotonic clock moves behind the previous value, the next click is accepted as a new runtime.

## Accessibility

The implementation uses Compose `clickable`, preserving indication, interaction, click semantics, and accessibility behavior. Provide `onClickLabel` and `role` for ambiguous targets such as icons.

```kotlin
Icon(
    imageVector = Icons.Default.Refresh,
    contentDescription = null,
    modifier = Modifier.bkThrottledClickable(
        onClickLabel = "Refresh",
        role = Role.Button,
    ) {
        refresh()
    },
)
```

## Catalog and verification

The catalog demonstrates the default 500 ms interval and a custom two-second interval with visible accepted-click counts.

Unit tests cover the first click, suppression, interval boundary, interval changes, and zero interval. Compose integration verification covers independent Modifiers, enabled changes, recomposition, latest callbacks, and accessibility semantics. Clock rollback behavior requires additional verification before Stable status.

## Excluded from the first release

- Trailing-edge execution and debounce
- Global throttle keys shared by multiple Modifiers
- Coroutine or suspend work execution
- Automatic disabling until work completes
- Click history and analytics
