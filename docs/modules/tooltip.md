# Tooltip

**Module:** `:components:tooltip`  
**Artifact:** `io.github.beez:tooltip`  
**Status:** Planned

## Goal

Provide Balloon-like anchored Compose tooltips with simple defaults and optional custom content.

## Target API

```kotlin
Modifier.bkTooltip("Edit your profile here")

Modifier.bkTooltip(
    placement = BeezKitTooltip.Placement.Bottom,
    dismissOnOutsideClick = true,
) {
    CustomTooltipContent()
}
```

## Required behavior

- Track anchor placement and choose a safe fallback near window edges.
- Dismiss on configured timeout, outside click, back press, anchor detach, or lifecycle stop.
- Handle scrolling and configuration changes without retaining coordinates or owners.
- Support arrow placement, custom content, accessibility focus, and one-tooltip-at-a-time policy.

## Verification

Test every placement, edge collision, scrolling, anchor removal, back/outside dismissal, accessibility focus, configuration change, and concurrent requests.

