# Stack Trace

**Module:** `:toolkit:stacktrace`  
**Artifact:** `io.github.beez:stacktrace`  
**Status:** Planned

## Goal

Log a supplied value together with the relevant call path up to the invocation point.

## Target API

```kotlin
BeezKitStackTrace.log(userId)

BeezKitStackTrace.log(
    value = userId,
    tag = "UpdateUser",
    maxDepth = 10,
)
```

## Required behavior

- Skip BeezKit implementation frames and optionally filter to host package prefixes.
- Avoid creating a stack trace when logging is disabled.
- Limit depth and formatted value size by default.
- Accept lazy messages and custom value formatters.
- Permit release no-op configuration and rate limiting.
- Never log credentials or personal data automatically.

## Verification

Test frame filtering, depth limits, disabled-mode allocation path, formatter failures, concurrent calls, and deterministic output formatting.

