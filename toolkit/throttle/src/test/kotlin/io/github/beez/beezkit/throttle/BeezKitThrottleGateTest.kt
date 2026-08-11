package io.github.beez.beezkit.throttle

import kotlin.time.Duration.Companion.milliseconds
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BeezKitThrottleGateTest {
    private var nowNanos: Long = 0L
    private val gate = BeezKitThrottleGate { nowNanos }

    @Test
    fun firstClickIsAccepted() {
        assertTrue(gate.tryAcquire(500.milliseconds))
    }

    @Test
    fun clickInsideIntervalIsSuppressed() {
        assertTrue(gate.tryAcquire(500.milliseconds))

        advanceBy(499)

        assertFalse(gate.tryAcquire(500.milliseconds))
    }

    @Test
    fun clickAtIntervalBoundaryIsAccepted() {
        assertTrue(gate.tryAcquire(500.milliseconds))

        advanceBy(500)

        assertTrue(gate.tryAcquire(500.milliseconds))
    }

    @Test
    fun updatedIntervalIsUsedWithoutResettingLastAcceptedClick() {
        assertTrue(gate.tryAcquire(1_000.milliseconds))
        advanceBy(400)

        assertTrue(gate.tryAcquire(250.milliseconds))
    }

    @Test
    fun zeroIntervalAcceptsEveryClick() {
        assertTrue(gate.tryAcquire(0.milliseconds))
        assertTrue(gate.tryAcquire(0.milliseconds))
    }

    private fun advanceBy(milliseconds: Long) {
        nowNanos += milliseconds * 1_000_000L
    }
}
