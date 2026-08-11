# Inspector

**Modules:** `:toolkit:inspector:{core,network,event,webview}`  
**Artifacts:** `io.github.beez:inspector-*`  
**Status:** Planned

## Goal

Display selected host-app diagnostic streams in a collapsible, draggable in-app floating panel. It must not require system overlay permission.

## Target API

```kotlin
BeezKitInspectorHost(
    config = BeezKitInspectorConfig(
        scopes = setOf(Network, Event, WebView, Measure),
    ),
) {
    AppNavigation()
}
```

Consumers should add collectors with `debugImplementation`.

## Core requirements

- Render inside the host application's Compose hierarchy.
- Support collapsed, expanded, moved, filtered, and cleared states.
- Keep bounded ring buffers with configurable item and byte limits.
- Store immutable diagnostic snapshots, never Activity/View/WebView references.
- Stop UI-owned work when the host leaves composition.
- Apply redaction before data reaches storage or UI.

## Network collector

- Begin with an adapter boundary; do not expose a networking library type from inspector core.
- Capture method, URL, timing, status, headers, and optionally bounded text bodies.
- Redact authorization, cookies, tokens, passwords, and configurable fields.
- Skip binary bodies by default.

## Event collector

- Accept structured name, timestamp, category, and bounded attributes.
- Define overflow and sampling policies.
- Avoid retaining arbitrary host objects as attribute values.

## WebView collector

- Attach explicitly to selected WebViews and detach safely.
- Capture console/navigation diagnostics without retaining the WebView.
- Document limitations caused by existing WebChromeClient/WebViewClient ownership.

## Verification

Test buffer eviction, redaction, large payloads, detach/reattach, multiple windows, configuration change, collector failures, release dependency exclusion, and memory leaks.

