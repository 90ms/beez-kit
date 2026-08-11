package io.github.beez.beezkit.throttle

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** Default interval used by [bkThrottledClickable]. */
public val BeezKitDefaultThrottleInterval: Duration = 500.milliseconds

/**
 * Makes this element clickable while accepting only the leading click in each [interval].
 *
 * Throttle state belongs to this modifier instance and survives recomposition. The underlying
 * Compose clickable modifier supplies indication, interaction, click semantics, and accessibility
 * behavior.
 */
public fun Modifier.bkThrottledClickable(
    interval: Duration = BeezKitDefaultThrottleInterval,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier {
    require(!interval.isNegative()) { "interval must not be negative" }

    return composed(
        inspectorInfo = {
            name = "bkThrottledClickable"
            properties["interval"] = interval
            properties["enabled"] = enabled
            properties["onClickLabel"] = onClickLabel
            properties["role"] = role
        },
    ) {
        val gate = remember { BeezKitThrottleGate(SystemClock::elapsedRealtimeNanos) }
        val currentOnClick = rememberUpdatedState(onClick)

        Modifier.clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
        ) {
            if (gate.tryAcquire(interval)) {
                currentOnClick.value()
            }
        }
    }
}

internal class BeezKitThrottleGate(
    private val nowNanos: () -> Long,
) {
    private var hasAcceptedClick: Boolean = false
    private var lastAcceptedNanos: Long = 0L

    fun tryAcquire(interval: Duration): Boolean {
        val now = nowNanos()
        val elapsed = now - lastAcceptedNanos
        val clockRestarted = now < lastAcceptedNanos

        if (!hasAcceptedClick || clockRestarted || elapsed >= interval.inWholeNanoseconds) {
            hasAcceptedClick = true
            lastAcceptedNanos = now
            return true
        }

        return false
    }
}
