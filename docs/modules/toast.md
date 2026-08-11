# Toast

**Module:** `:components:toast`  
**Artifact:** `io.github.beez:toast`  
**Status:** Planned

## Goal

Provide customizable foreground in-app messages with Toast-like usage. This is a Compose overlay, not a custom `android.widget.Toast`.

## Target API

```kotlin
BeezKitToast.success("Saved")

BeezKitToast.show(
    value = "Saved",
    duration = 2.seconds,
    offset = DpOffset(0.dp, 24.dp),
)
```

## Required behavior

- Require one lifecycle-safe host installation, then allow one-line calls.
- Provide success, error, warning, info, and custom visual styles.
- Define queue, replace-current, and drop policies.
- Bound pending requests and dispose timers with the active host.
- Support accessibility announcements and reduced-motion behavior.
- Do not claim background/system Toast behavior.

## Verification

Test queue ordering, replacement, duration, offsets, multiple calls, host recreation, no-host behavior, accessibility, and bounded pending work.

