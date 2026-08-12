package io.github.beez.beezkit.skeleton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.TimeSource

@Composable
internal fun rememberBeezKitSkeletonVisibility(
    loading: Boolean,
    minimumVisibleDuration: Duration,
): Boolean {
    require(!minimumVisibleDuration.isNegative() && minimumVisibleDuration.isFinite()) {
        "minimumVisibleDuration must be non-negative and finite"
    }

    var visible by remember { mutableStateOf(loading) }
    var shownAt by remember {
        mutableStateOf(if (loading) TimeSource.Monotonic.markNow() else null)
    }

    LaunchedEffect(loading, minimumVisibleDuration) {
        if (loading) {
            if (!visible) shownAt = TimeSource.Monotonic.markNow()
            visible = true
            return@LaunchedEffect
        }

        if (!visible) return@LaunchedEffect

        val remaining = BeezKitSkeletonVisibilityPolicy.remaining(
            minimumVisibleDuration = minimumVisibleDuration,
            elapsed = shownAt?.elapsedNow() ?: minimumVisibleDuration,
        )
        if (remaining.isPositive()) delay(remaining)

        visible = false
        shownAt = null
    }

    return visible
}

internal object BeezKitSkeletonVisibilityPolicy {
    fun remaining(
        minimumVisibleDuration: Duration,
        elapsed: Duration,
    ): Duration {
        require(!minimumVisibleDuration.isNegative() && minimumVisibleDuration.isFinite())
        val safeElapsed = elapsed.coerceAtLeast(Duration.ZERO)
        return (minimumVisibleDuration - safeElapsed).coerceAtLeast(Duration.ZERO)
    }
}
