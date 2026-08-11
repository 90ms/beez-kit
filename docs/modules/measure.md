# Measure

**Module:** `:toolkit:measure`  
**Artifact:** `io.github.beez:measure`  
**Status:** Planned

## Goal

Measure elapsed time between related code points and expose bounded results to logging or Inspector consumers.

## Target API

```kotlin
val result = BeezKitMeasure.trace("load-user") {
    repository.loadUser()
}

val span = BeezKitMeasure.start("load-user")
span.end()
```

## Required behavior

- Use monotonic elapsed time.
- Make block/span APIs primary; same-tag `start/end` is convenience only.
- Assign a unique identifier to every span so concurrent identical tags cannot collide.
- Record success, failure, cancellation, and elapsed duration without swallowing exceptions.
- Bound retained history and allow collection to be disabled.
- Provide synchronous and suspend-friendly APIs.

## Verification

Test nested spans, identical concurrent tags, exceptions, cancellation, clock abstraction, history eviction, and disabled mode.

