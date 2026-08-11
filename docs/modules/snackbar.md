# Snackbar

**Module:** `:components:snackbar`  
**Artifact:** `io.github.beez:snackbar`  
**Status:** Planned

## Goal

Provide branded Compose snackbars with one-line calls and optional actions, offsets, and result handling.

## Target API

```kotlin
BeezKitSnackbar.error("Network error")

val result = BeezKitSnackbar.show(
    value = "Deleted",
    actionLabel = "Undo",
    duration = BeezKitSnackbar.Duration.Long,
)
```

## Required behavior

- Build on a lifecycle-owned host/coordinator.
- Support default styles, custom content, action results, dismissal, duration, and offset.
- Serialize or replace requests according to an explicit policy.
- Never retain action callbacks beyond completion or dismissal.
- Preserve Material semantics and accessibility timing.

## Verification

Test action/dismiss results, cancellation, queue policy, host recreation, custom content, accessibility, and callback release.

