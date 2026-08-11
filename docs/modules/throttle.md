# Throttle

**Module:** `:toolkit:throttle`  
**Artifact:** `io.github.beez:throttle`  
**Status:** Planned

## Goal

Provide Compose Modifier APIs that prevent accidental repeated actions with a safe default interval and an optional custom interval.

## Target API

```kotlin
Modifier.bkThrottledClickable { submit() }

Modifier.bkThrottledClickable(
    interval = 750.milliseconds,
    enabled = true,
) { submit() }
```

## Required behavior

- Default to leading-edge execution: accept the first click and suppress subsequent clicks during the interval.
- Preserve state across recomposition without sharing state between Modifier instances.
- Support `enabled`, interaction indication, semantics, and accessibility click behavior.
- Use monotonic time.
- Prefer `Modifier.Node` for stateful implementation and allocation control.
- Do not retain the host composable or Activity after detachment.

## Verification

Test the first click, suppressed clicks, interval boundary, parameter updates, independent Modifier instances, detach/reattach, and accessibility semantics.

