package io.github.beez.beezkit.skeleton

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawOutline
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.stateDescription
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration

/** Draws a loading placeholder in place of the decorated content. */
public fun Modifier.bkSkeleton(
    visible: Boolean,
    shape: Shape = BeezKitSkeletonDefaults.shape,
    colors: BeezKitSkeletonColors = BeezKitSkeletonDefaults.colors(),
    animation: BeezKitSkeletonAnimation = BeezKitSkeletonDefaults.animation,
): Modifier = skeletonElement(
    visible = visible,
    shape = shape,
    colors = colors,
    animation = animation,
    sharedProgress = null,
)

/** Receiver used to apply a shared skeleton state and animation to a group of children. */
public interface BeezKitSkeletonScope {
    public fun Modifier.bkSkeleton(
        shape: Shape? = null,
        colors: BeezKitSkeletonColors? = null,
    ): Modifier
}

/** Provides one loading state and animation clock to all skeleton children in [content]. */
@Composable
public fun BeezKitSkeletonScope(
    visible: Boolean,
    modifier: Modifier = Modifier,
    style: BeezKitSkeletonStyle = BeezKitSkeletonDefaults.style(),
    minimumVisibleDuration: Duration = Duration.ZERO,
    loadingDescription: String? = null,
    content: @Composable BeezKitSkeletonScope.() -> Unit,
) {
    val effectiveVisible = rememberBeezKitSkeletonVisibility(visible, minimumVisibleDuration)
    val motionEnabled = rememberBeezKitMotionEnabled()
    val progress = rememberBeezKitSkeletonProgress(
        visible = effectiveVisible && motionEnabled,
        animation = style.animation,
    )
    val scope = remember(effectiveVisible, style, progress, motionEnabled) {
        BeezKitSkeletonScopeImpl(
            visible = effectiveVisible,
            style = style,
            progress = progress,
            motionEnabled = motionEnabled,
        )
    }

    Box(
        modifier = modifier.loadingSemantics(effectiveVisible, loadingDescription),
    ) {
        scope.content()
    }
}

/** Switches between a structured [skeleton] slot and the host [content]. */
@Composable
public fun BeezKitSkeletonContainer(
    loading: Boolean,
    modifier: Modifier = Modifier,
    style: BeezKitSkeletonStyle = BeezKitSkeletonDefaults.style(),
    minimumVisibleDuration: Duration = Duration.ZERO,
    transition: BeezKitSkeletonTransition = BeezKitSkeletonTransition.None,
    loadingDescription: String? = null,
    skeleton: @Composable BeezKitSkeletonScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val visible = rememberBeezKitSkeletonVisibility(loading, minimumVisibleDuration)
    val motionEnabled = rememberBeezKitMotionEnabled()
    val containerModifier = modifier.loadingSemantics(visible, loadingDescription)

    when {
        transition is BeezKitSkeletonTransition.Crossfade && motionEnabled -> {
            Crossfade(
                targetState = visible,
                modifier = containerModifier,
                animationSpec = tween(durationMillis = transition.duration.asMillisInt()),
                label = "BeezKitSkeletonContainer",
            ) { showSkeleton ->
                Box(modifier = Modifier.blockWhen(showSkeleton != visible)) {
                    if (showSkeleton) {
                        BeezKitSkeletonScope(
                            visible = true,
                            style = style,
                            content = skeleton,
                        )
                    } else {
                        content()
                    }
                }
            }
        }

        visible -> BeezKitSkeletonScope(
            visible = true,
            modifier = containerModifier,
            style = style,
            content = skeleton,
        )

        else -> Box(modifier = containerModifier) { content() }
    }
}

private class BeezKitSkeletonScopeImpl(
    private val visible: Boolean,
    private val style: BeezKitSkeletonStyle,
    private val progress: State<Float>,
    private val motionEnabled: Boolean,
) : BeezKitSkeletonScope {
    override fun Modifier.bkSkeleton(
        shape: Shape?,
        colors: BeezKitSkeletonColors?,
    ): Modifier = skeletonElement(
        visible = visible,
        shape = shape ?: style.shape,
        colors = colors ?: style.colors,
        animation = if (motionEnabled) style.animation else BeezKitSkeletonAnimation.Static,
        sharedProgress = progress,
    )
}

private fun Modifier.skeletonElement(
    visible: Boolean,
    shape: Shape,
    colors: BeezKitSkeletonColors,
    animation: BeezKitSkeletonAnimation,
    sharedProgress: State<Float>?,
): Modifier = composed(
    inspectorInfo = {
        name = "bkSkeleton"
        properties["visible"] = visible
        properties["shape"] = shape
        properties["colors"] = colors
        properties["animation"] = animation
    },
) {
    if (!visible) return@composed Modifier

    val motionEnabled = rememberBeezKitMotionEnabled()
    val effectiveAnimation = if (motionEnabled) animation else BeezKitSkeletonAnimation.Static
    val ownProgress = rememberBeezKitSkeletonProgress(
        visible = visible && sharedProgress == null,
        animation = effectiveAnimation,
    )
    val progress = sharedProgress ?: ownProgress
    val layoutDirection = LocalLayoutDirection.current

    Modifier
        .drawWithCache {
            val outline = shape.createOutline(size, layoutDirection, this)
            onDrawWithContent {
                drawSkeleton(
                    outline = outline,
                    size = size,
                    colors = colors,
                    animation = effectiveAnimation,
                    progress = progress.value,
                    layoutDirectionRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl,
                )
            }
        }
        .clearAndSetSemantics { }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                }
            }
        }
}

@Composable
private fun rememberBeezKitSkeletonProgress(
    visible: Boolean,
    animation: BeezKitSkeletonAnimation,
): State<Float> {
    if (!visible || animation is BeezKitSkeletonAnimation.Static) {
        return remember { mutableFloatStateOf(0f) }
    }

    val transition = rememberInfiniteTransition(label = "BeezKitSkeleton")
    val duration = when (animation) {
        is BeezKitSkeletonAnimation.Pulse -> animation.duration
        is BeezKitSkeletonAnimation.Shimmer -> animation.duration
        BeezKitSkeletonAnimation.Static -> Duration.ZERO
    }
    val repeatMode = if (animation is BeezKitSkeletonAnimation.Pulse) {
        RepeatMode.Reverse
    } else {
        RepeatMode.Restart
    }

    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = duration.asMillisInt(),
                easing = LinearEasing,
            ),
            repeatMode = repeatMode,
        ),
        label = "BeezKitSkeletonProgress",
    )
}

private fun DrawScope.drawSkeleton(
    outline: androidx.compose.ui.graphics.Outline,
    size: androidx.compose.ui.geometry.Size,
    colors: BeezKitSkeletonColors,
    animation: BeezKitSkeletonAnimation,
    progress: Float,
    layoutDirectionRtl: Boolean,
) {
    when (animation) {
        BeezKitSkeletonAnimation.Static -> drawOutline(outline, color = colors.base)

        is BeezKitSkeletonAnimation.Pulse -> {
            val alpha = animation.minAlpha +
                ((animation.maxAlpha - animation.minAlpha) * progress)
            drawOutline(outline, color = colors.base, alpha = alpha)
        }

        is BeezKitSkeletonAnimation.Shimmer -> {
            drawOutline(outline, color = colors.base)
            val vector = animation.direction.vector(animation.angleDegrees, layoutDirectionRtl)
            val travel = size.width + size.height
            val centerDistance = (-animation.widthFraction +
                progress * (1f + animation.widthFraction * 2f)) * travel
            val halfBand = animation.widthFraction * travel / 2f
            val center = Offset(size.width / 2f, size.height / 2f) + vector * centerDistance
            val start = center - vector * halfBand
            val end = center + vector * halfBand
            drawOutline(
                outline = outline,
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, colors.highlight, Color.Transparent),
                    start = start,
                    end = end,
                ),
            )
        }
    }
}

private fun BeezKitSkeletonDirection.vector(
    angleDegrees: Float,
    layoutDirectionRtl: Boolean,
): Offset {
    val baseDegrees = when (this) {
        BeezKitSkeletonDirection.StartToEnd -> if (layoutDirectionRtl) 180f else 0f
        BeezKitSkeletonDirection.EndToStart -> if (layoutDirectionRtl) 0f else 180f
        BeezKitSkeletonDirection.TopToBottom -> 90f
        BeezKitSkeletonDirection.BottomToTop -> 270f
    }
    val radians = Math.toRadians((baseDegrees + angleDegrees).toDouble())
    return Offset(cos(radians).toFloat(), sin(radians).toFloat())
}

private fun Modifier.loadingSemantics(
    visible: Boolean,
    description: String?,
): Modifier = if (visible && description != null) {
    clearAndSetSemantics { stateDescription = description }
} else if (visible) {
    clearAndSetSemantics { }
} else {
    this
}

private fun Modifier.blockWhen(blocked: Boolean): Modifier = if (blocked) {
    clearAndSetSemantics { }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                }
            }
        }
} else {
    this
}

@Composable
private fun rememberBeezKitMotionEnabled(): Boolean {
    val context = rememberCoroutineScope().coroutineContext
    return (context[MotionDurationScale]?.scaleFactor ?: 1f) > 0f
}

private fun Duration.asMillisInt(): Int = inWholeMilliseconds.toInt()
