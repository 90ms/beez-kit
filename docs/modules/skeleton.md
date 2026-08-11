# Skeleton

**Module:** `:components:skeleton`  
**Artifact:** `io.github.beez:skeleton`  
**Status:** Planned

## Goal

Apply a skeleton state to a Compose component with one Modifier call and provide explicit primitives for structured screen placeholders.

## Target API

```kotlin
Modifier.bkSkeleton(visible = isLoading)

BeezKitSkeletonContainer(
    loading = isLoading,
    skeleton = { UserScreenSkeleton() },
) {
    UserScreen(user)
}
```

## Required behavior

- Preserve the measured shape of a decorated component.
- Support static, pulse, and shimmer modes with customizable shape and colors.
- Avoid a coroutine or infinite transition per item in large lists; share animation state where practical.
- Respect reduced-motion configuration.
- Prevent hidden real content from being announced or clicked while loading.
- Document that arbitrary screens cannot be semantically converted into structured skeletons automatically.

## Verification

Test visibility transitions, shape, list usage, shared animation, disabled motion, semantics, click blocking, recomposition, and detach cleanup.

