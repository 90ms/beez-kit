# Safety guidelines

- Do not store Activity, View, Window, WebView, LayoutCoordinates, or composable callbacks in process-global state.
- Make ownership explicit for scopes, listeners, timers, and collectors; release them on detach.
- Use monotonic clocks for elapsed time and unique tokens for concurrent spans.
- Bound every queue, retained history, payload, formatted value, and retry loop.
- Redact secrets before storing diagnostic data. Treat headers, cookies, bodies, and event attributes as sensitive.
- Keep Inspector integrations debug-only by default and ensure release builds do not pull them transitively.
- Avoid reflection and broad keep rules; add narrow consumer rules only when proven necessary.
- Test overflow, cancellation, failures, concurrent calls, and disabled/no-host modes.

