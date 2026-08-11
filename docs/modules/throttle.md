# Throttle

**Module:** `:toolkit:throttle`  
**Artifact:** `io.github.beez:throttle`  
**Status:** Experimental

## Goal

Provide Compose Modifier APIs that prevent accidental repeated actions with a safe default interval and an optional custom interval.

## Public API

```kotlin
Modifier.bkThrottledClickable { submit() }

Modifier.bkThrottledClickable(
    interval = 750.milliseconds,
    enabled = true,
    onClickLabel = "Submit",
    role = Role.Button,
) { submit() }
```

The default interval is exposed as `BeezKitDefaultThrottleInterval` and is `500.milliseconds`.

## Required behavior

- Default to leading-edge execution: accept the first click and suppress subsequent clicks during the interval.
- Preserve state across recomposition without sharing state between Modifier instances.
- Support `enabled`, interaction indication, semantics, and accessibility click behavior.
- Use monotonic time.
- Do not retain the host composable or Activity after detachment.

## Current implementation

- Uses Compose `clickable`, preserving its indication, interaction, semantics, and accessibility behavior.
- Keeps one small gate per composed Modifier instance and reads the latest callback after recomposition.
- Reads `SystemClock.elapsedRealtimeNanos()` only when a click is received.
- Rejects negative intervals; a zero interval accepts every click.
- Does not start coroutines, register listeners, or retain an Android owner.

## Verification

Unit tests cover the first click, suppressed clicks, interval boundary, interval updates, and zero interval. Compose integration verification must cover independent Modifier instances, enabled changes, recomposition, and accessibility semantics before Stable status.
