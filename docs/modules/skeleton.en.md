# Skeleton

[한국어](skeleton.md) | [English](skeleton.en.md)

**Module:** `:components:skeleton`
**Artifact:** `io.github.beez:skeleton`
**Status:** Experimental

## Purpose

Skeleton displays a loading placeholder while preserving the measured shape of a Compose component. At screen level, it safely switches between a structured placeholder supplied by the host and the real content.

The host application remains the sole owner of data loading, success, failure, retry, and caching. BeezKit only derives presentation from the loading state passed to it. It does not inspect an arbitrary screen or infer a skeleton structure for content that has not been composed.

## Installation

Add the Skeleton artifact to the host application module.

```kotlin
dependencies {
    implementation("io.github.beez:skeleton:<version>")
}
```

Skeleton exposes Compose UI types but does not depend on Material or Material 3. A host can pass colors and shapes from its own design system.

## Choosing an API

| Use case | API | Choose it when |
| --- | --- | --- |
| One Text, Image, or Card | `Modifier.bkSkeleton` | The placeholder should preserve the component's measured size and position |
| Several placeholders in one region | `BeezKitSkeletonScope` | Children should share loading state, style, and one animation clock |
| A whole screen or a loading layout unlike the real content | `BeezKitSkeletonContainer` | Explicit skeleton and content slots need to be switched |

Start with the smallest API. Apply the Modifier to one element, introduce a Scope when several elements animate together, and use a Container only when the loading and content layouts differ.

The host would normally collect its state from a `ViewModel` in a lifecycle-aware way.

```kotlin
@Composable
fun ProfileRoute(viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(uiState)
}
```

BeezKit receives only a value such as `uiState.isLoading`; it never runs a repository or suspend function.

## Basic API

Use `Modifier.bkSkeleton` for a single component. The placeholder uses the component's measured size and the requested shape.

```kotlin
Text(
    text = user?.name.orEmpty(),
    modifier = Modifier
        .widthIn(min = 120.dp)
        .bkSkeleton(visible = uiState.isLoading),
)

AsyncImage(
    model = user?.imageUrl,
    contentDescription = null,
    modifier = Modifier
        .size(64.dp)
        .bkSkeleton(
            visible = uiState.isLoading,
            shape = CircleShape,
        ),
)
```

```kotlin
fun Modifier.bkSkeleton(
    visible: Boolean,
    shape: Shape = BeezKitSkeletonDefaults.shape,
    colors: BeezKitSkeletonColors = BeezKitSkeletonDefaults.colors(),
    animation: BeezKitSkeletonAnimation = BeezKitSkeletonDefaults.animation,
): Modifier
```

- When `visible` is `false`, the Modifier does not alter measurement, drawing, semantics, or input.
- When `visible` is `true`, it preserves the content's measured size and draws the placeholder over it.
- It blocks input and removes the decorated content from the accessibility tree while loading.
- `shape` affects skeleton drawing and clipping only; it does not change the host content's shape.
- Each standalone Modifier owns only its presentation state. It does not create a timer, process-global state, or a separate coroutine scope.

### Content without a measured size

Skeleton uses the size measured by the decorated component. A `Text` receiving an empty string can measure to zero width, so specify a minimum size while loading.

```kotlin
Text(
    text = user?.name.orEmpty(),
    modifier = Modifier
        .widthIn(min = 120.dp)
        .heightIn(min = 20.dp)
        .bkSkeleton(visible = uiState.isLoading),
)
```

Prefer explicit `width`, `height`, `size`, or constraints over fake whitespace or transparent content.

## Region API

Use `BeezKitSkeletonScope` when several placeholders share one loading state and animation.

```kotlin
BeezKitSkeletonScope(
    visible = uiState.isLoading,
    minimumVisibleDuration = 300.milliseconds,
) {
    Row {
        Box(Modifier.size(64.dp).bkSkeleton(shape = CircleShape))
        Column {
            Box(Modifier.size(width = 140.dp, height = 20.dp).bkSkeleton())
            Box(Modifier.size(width = 200.dp, height = 16.dp).bkSkeleton())
        }
    }
}
```

```kotlin
@Composable
fun BeezKitSkeletonScope(
    visible: Boolean,
    modifier: Modifier = Modifier,
    style: BeezKitSkeletonStyle = BeezKitSkeletonDefaults.style(),
    minimumVisibleDuration: Duration = Duration.ZERO,
    loadingDescription: String? = null,
    content: @Composable BeezKitSkeletonScope.() -> Unit,
)
```

- The receiver version of `Modifier.bkSkeleton()` uses the nearest Scope's visibility and style.
- A child may override its shape and colors. Animation type and speed belong to the Scope so all children remain synchronized.
- The Scope shares one animation progress value with every moving child, including children in a `LazyColumn`.
- A child performs only the drawing calculation needed for its own measured size.
- No infinite animation runs while the Scope is hidden or motion is disabled.
- A nested Scope uses its own nearest state and style.
- `minimumVisibleDuration` creates at most one timer per Scope, never one timer per child Modifier.

## Screen API

Use `BeezKitSkeletonContainer` when the real screen and its placeholder have different structures.

```kotlin
BeezKitSkeletonContainer(
    loading = uiState.isLoading,
    minimumVisibleDuration = 300.milliseconds,
    skeleton = { ProfileScreenSkeleton() },
) {
    ProfileScreenContent(uiState.profile)
}
```

```kotlin
@Composable
fun BeezKitSkeletonContainer(
    loading: Boolean,
    modifier: Modifier = Modifier,
    style: BeezKitSkeletonStyle = BeezKitSkeletonDefaults.style(),
    minimumVisibleDuration: Duration = Duration.ZERO,
    transition: BeezKitSkeletonTransition = BeezKitSkeletonTransition.None,
    loadingDescription: String? = null,
    skeleton: @Composable BeezKitSkeletonScope.() -> Unit,
    content: @Composable () -> Unit,
)
```

- The default `None` transition composes only the current skeleton or content slot.
- The Container does not precompose hidden real content, so effects in that content do not start while loading.
- `Crossfade` may compose both slots during the transition. The inactive slot has no input or semantics and leaves composition after the transition.
- Every placeholder in the skeleton slot shares the Container's Scope and animation progress.

### Complete screen example

```kotlin
@Composable
fun ProfileScreen(uiState: ProfileUiState) {
    BeezKitSkeletonContainer(
        loading = uiState.isLoading,
        minimumVisibleDuration = 300.milliseconds,
        transition = BeezKitSkeletonTransition.Crossfade(),
        loadingDescription = "Loading profile",
        skeleton = { ProfileSkeleton() },
    ) {
        ProfileContent(profile = uiState.profile)
    }
}

@Composable
private fun BeezKitSkeletonScope.ProfileSkeleton() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(64.dp).bkSkeleton(shape = CircleShape))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(width = 140.dp, height = 20.dp).bkSkeleton())
            Box(Modifier.size(width = 220.dp, height = 16.dp).bkSkeleton())
        }
    }
}
```

## Animation

```kotlin
sealed interface BeezKitSkeletonAnimation {
    data object Static : BeezKitSkeletonAnimation

    data class Pulse(
        val duration: Duration = 1_000.milliseconds,
        val minAlpha: Float = 0.6f,
        val maxAlpha: Float = 1f,
    ) : BeezKitSkeletonAnimation

    data class Shimmer(
        val duration: Duration = 1_200.milliseconds,
        val widthFraction: Float = 0.35f,
        val angleDegrees: Float = 20f,
        val direction: BeezKitSkeletonDirection =
            BeezKitSkeletonDirection.StartToEnd,
    ) : BeezKitSkeletonAnimation
}
```

| Type | Behavior |
| --- | --- |
| `Static` | Draws only the base color without motion |
| `Pulse` | Repeats between `minAlpha` and `maxAlpha` |
| `Shimmer` | Moves a highlight band over the base color |

Directions are `StartToEnd`, `EndToStart`, `TopToBottom`, and `BottomToTop`. Horizontal directions follow `LayoutDirection`, so the physical direction of `StartToEnd` reverses in RTL.

- Moving animation durations must be from 1 ms through `Int.MAX_VALUE` ms.
- Alpha values must be within `0f..1f`, with `minAlpha <= maxAlpha`.
- `widthFraction` must satisfy `0f < widthFraction <= 1f`.
- `angleDegrees` must be finite.
- Invalid public input throws `IllegalArgumentException` at the call site.
- Animation invalidates drawing without remeasuring or replacing the layout every frame.

## Colors and style

```kotlin
@Immutable
data class BeezKitSkeletonColors(
    val base: Color,
    val highlight: Color,
)

@Immutable
data class BeezKitSkeletonStyle(
    val colors: BeezKitSkeletonColors,
    val shape: Shape,
    val animation: BeezKitSkeletonAnimation,
)
```

```kotlin
BeezKitSkeletonScope(
    visible = uiState.isLoading,
    style = BeezKitSkeletonDefaults.style(
        colors = BeezKitSkeletonDefaults.colors(
            base = MaterialTheme.colorScheme.surfaceVariant,
            highlight = MaterialTheme.colorScheme.surface,
        ),
        animation = BeezKitSkeletonAnimation.Shimmer(
            duration = 900.milliseconds,
        ),
    ),
) {
    ProfileSkeleton()
}
```

- The default shape is `RoundedCornerShape(8.dp)`.
- The default animation is `Shimmer()`.
- Default colors are opaque neutral colors independent of Material.
- A host integrates its theme by passing `Color` values; Material types are not exposed by the library API.
- Color, style, and animation models are immutable values and do not retain an Activity, Context, View, or callback.

## Minimum visible duration

`minimumVisibleDuration` prevents a brief loading result from flashing. It neither ends real loading nor imposes a maximum loading time.

```text
Hidden
  loading=true  -> Visible

Visible
  loading=true  -> Visible
  loading=false + minimum elapsed     -> Hidden
  loading=false + minimum not elapsed -> WaitingToHide

WaitingToHide
  loading=true  -> Visible
  timer elapsed -> Hidden
```

- Loading longer than the minimum hides the skeleton immediately when loading ends.
- A new `loading = true` while waiting cancels the scheduled hide.
- An obsolete timer can never overwrite the latest loading state.
- Elapsed time uses a monotonic clock and is unaffected by wall-clock changes.
- The duration must be non-negative and finite.
- Removing the Scope or Container from composition cancels its pending timer.
- Animation, timer, and transition progress are not restored after configuration or process recreation. Restoring business state is the host's responsibility.

A fixed timeout, a `loader` block, and an imperative `showFor()` API are intentionally excluded because they could hide a placeholder before the host operation has completed.

## Transition

```kotlin
sealed interface BeezKitSkeletonTransition {
    data object None : BeezKitSkeletonTransition

    data class Crossfade(
        val duration: Duration = 200.milliseconds,
    ) : BeezKitSkeletonTransition
}
```

- `None` switches slots immediately and is the default.
- A crossfade duration must be from 1 ms through `Int.MAX_VALUE` ms.
- If loading restarts during a crossfade, the current transition is cancelled and moves toward the latest state.
- The logically inactive slot has no input or semantics regardless of opacity.
- Crossfade is skipped when the system requests reduced motion.

## Lifecycle and asynchronous safety

- Skeleton does not accept or run repositories, suspend blocks, `Flow`, retries, or network requests.
- Timers and animations are owned by the current composition.
- Removing a Scope, Container, or Modifier cancels or releases its timer, animation, and callbacks.
- The module creates no process-global coroutine scope, animation registry, or content registry.
- It never stores an Activity, Context, View, Window, layout coordinates, composable lambda, or coroutine scope in global state.
- Rapid `true -> false -> true` changes cannot be overwritten by cancelled work.
- Parameter changes update local behavior without sharing state between screens or owners.

## Accessibility and input

- While loading, real content is removed from the accessibility tree and click, long-click, and pointer input are blocked.
- Individual skeleton blocks do not appear as meaningless accessibility elements.
- When `loadingDescription` is non-null, the Scope or Container exposes that loading state once rather than repeating it for every placeholder.
- Real content semantics and input are restored after loading.
- Two slots cannot both be announced or operated during a crossfade.
- Under reduced motion, `Pulse` and `Shimmer` fall back to `Static`, and `Crossfade` becomes an immediate switch.

## Performance and memory

- Placeholders are drawn without remeasurement on every animation frame.
- A standalone Modifier owns animation state only when visible.
- A Scope or Container shares one progress value across all children, including lazy-list items.
- Brush and geometry data remain local to the current measured node and are discarded with it.
- The module has no queue, history, or process-wide cache.
- Hidden skeletons produce no animation frames and retain no timer coroutine.
- Multiple screens and Scopes remain independent and are never connected by a display tag or global key.

## Catalog and verification

The catalog demonstrates the real public API for:

- Text, circular image, and rounded card placeholders
- `Static`, `Pulse`, and `Shimmer`
- Animation speed, colors, shape, direction, and shimmer width
- A list of placeholders sharing one Scope
- Screen-level Container with immediate and crossfade transitions
- Fast loading with `minimumVisibleDuration`
- Repeated loading changes, reduced motion, and loading descriptions

Unit and Compose UI verification covers visibility, measurement, style updates, monotonic timing, cancellation, rapid state reversals, shared animation, detach cleanup, semantics, input blocking, crossfade isolation, reduced motion, independent owners, and invalid inputs.

## Excluded from the first release

- Automatic skeleton generation by inspecting a screen
- XML View and Android `View` adapters
- Data loading, retry, error UI, and cache management
- A fixed-time `showFor()` API
- Process-global theme or animation configuration
- Custom animation callbacks and direct gradient-stop editing
- Automatic placeholder width and height inference
- Placeholder bitmap generation or caching
