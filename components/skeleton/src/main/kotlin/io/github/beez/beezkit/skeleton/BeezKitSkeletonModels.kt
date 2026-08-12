package io.github.beez.beezkit.skeleton

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Immutable
public data class BeezKitSkeletonColors(
    public val base: Color,
    public val highlight: Color,
)

@Immutable
public data class BeezKitSkeletonStyle(
    public val colors: BeezKitSkeletonColors,
    public val shape: Shape,
    public val animation: BeezKitSkeletonAnimation,
)

@Immutable
public sealed interface BeezKitSkeletonAnimation {
    public data object Static : BeezKitSkeletonAnimation

    @Immutable
    public data class Pulse(
        public val duration: Duration = 1.seconds,
        public val minAlpha: Float = 0.6f,
        public val maxAlpha: Float = 1f,
    ) : BeezKitSkeletonAnimation {
        init {
            requireAnimationDuration(duration)
            require(minAlpha in 0f..1f) { "minAlpha must be between 0 and 1" }
            require(maxAlpha in 0f..1f) { "maxAlpha must be between 0 and 1" }
            require(minAlpha <= maxAlpha) { "minAlpha must not exceed maxAlpha" }
        }
    }

    @Immutable
    public data class Shimmer(
        public val duration: Duration = 1_200.milliseconds,
        public val widthFraction: Float = 0.35f,
        public val angleDegrees: Float = 20f,
        public val direction: BeezKitSkeletonDirection = BeezKitSkeletonDirection.StartToEnd,
    ) : BeezKitSkeletonAnimation {
        init {
            requireAnimationDuration(duration)
            require(widthFraction > 0f && widthFraction <= 1f) {
                "widthFraction must be greater than 0 and at most 1"
            }
            require(angleDegrees.isFinite()) { "angleDegrees must be finite" }
        }
    }
}

public enum class BeezKitSkeletonDirection {
    StartToEnd,
    EndToStart,
    TopToBottom,
    BottomToTop,
}

@Immutable
public sealed interface BeezKitSkeletonTransition {
    public data object None : BeezKitSkeletonTransition

    @Immutable
    public data class Crossfade(
        public val duration: Duration = 200.milliseconds,
    ) : BeezKitSkeletonTransition {
        init {
            requireAnimationDuration(duration)
        }
    }
}

public object BeezKitSkeletonDefaults {
    public val shape: Shape = RoundedCornerShape(8.dp)

    public val animation: BeezKitSkeletonAnimation = BeezKitSkeletonAnimation.Shimmer()

    public fun colors(
        base: Color = Color(0xFFE1E4E8),
        highlight: Color = Color(0xFFF4F5F7),
    ): BeezKitSkeletonColors = BeezKitSkeletonColors(
        base = base,
        highlight = highlight,
    )

    public fun style(
        colors: BeezKitSkeletonColors = colors(),
        shape: Shape = this.shape,
        animation: BeezKitSkeletonAnimation = this.animation,
    ): BeezKitSkeletonStyle = BeezKitSkeletonStyle(
        colors = colors,
        shape = shape,
        animation = animation,
    )
}

private fun requireAnimationDuration(duration: Duration) {
    require(
        duration.isFinite() && duration.inWholeMilliseconds in 1L..Int.MAX_VALUE.toLong(),
    ) {
        "duration must be between 1 millisecond and ${Int.MAX_VALUE} milliseconds"
    }
}
