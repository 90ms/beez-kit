package io.github.beez.beezkit.skeleton

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BeezKitSkeletonModelsTest {
    @Test
    fun pulseRejectsInvalidAlphaRange() {
        assertThrows(IllegalArgumentException::class.java) {
            BeezKitSkeletonAnimation.Pulse(minAlpha = 0.8f, maxAlpha = 0.4f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BeezKitSkeletonAnimation.Pulse(minAlpha = -0.1f)
        }
    }

    @Test
    fun movingAnimationsRejectNonPositiveDuration() {
        assertThrows(IllegalArgumentException::class.java) {
            BeezKitSkeletonAnimation.Pulse(duration = 0.milliseconds)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BeezKitSkeletonAnimation.Shimmer(duration = (-1).milliseconds)
        }
    }

    @Test
    fun shimmerRejectsInvalidGeometry() {
        assertThrows(IllegalArgumentException::class.java) {
            BeezKitSkeletonAnimation.Shimmer(widthFraction = 0f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BeezKitSkeletonAnimation.Shimmer(widthFraction = 1.1f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BeezKitSkeletonAnimation.Shimmer(angleDegrees = Float.NaN)
        }
    }

    @Test
    fun crossfadeRejectsNonPositiveDuration() {
        assertThrows(IllegalArgumentException::class.java) {
            BeezKitSkeletonTransition.Crossfade(duration = 0.seconds)
        }
    }

    @Test
    fun visibilityPolicyReturnsOnlyUnelapsedMinimum() {
        assertEquals(
            700.milliseconds,
            BeezKitSkeletonVisibilityPolicy.remaining(1.seconds, 300.milliseconds),
        )
        assertEquals(
            0.milliseconds,
            BeezKitSkeletonVisibilityPolicy.remaining(1.seconds, 2.seconds),
        )
        assertEquals(
            1.seconds,
            BeezKitSkeletonVisibilityPolicy.remaining(1.seconds, (-1).seconds),
        )
    }
}
