package io.github.beez.beezkit.measure

import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.time.Duration.Companion.milliseconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BeezKitMeasureRuntimeTest {
    private var nowNanos: Long = 0L

    @Test
    fun traceReturnsBlockValueAndReportsSuccess() {
        val reported = mutableListOf<BeezKitMeasureRecord>()
        val runtime = runtime(reporter = BeezKitMeasureReporter { reported += it })

        val value = runtime.trace("load-user", mapOf("source" to "detail")) {
            advanceBy(25)
            "user"
        }

        assertEquals("user", value)
        assertEquals(1, reported.size)
        assertEquals(25.milliseconds, reported.single().duration)
        assertEquals(BeezKitMeasureStatus.Success, reported.single().status)
        assertEquals(mapOf("source" to "detail"), reported.single().attributes)
    }

    @Test
    fun traceReportsFailureAndRethrowsSameError() {
        val reported = mutableListOf<BeezKitMeasureRecord>()
        val runtime = runtime(reporter = BeezKitMeasureReporter { reported += it })
        val expected = IllegalStateException("failed")

        val thrown = try {
            runtime.trace("failure", emptyMap()) { throw expected }
            fail("Expected trace to throw")
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertSame(expected, thrown)
        assertSame(expected, reported.single().error)
        assertEquals(BeezKitMeasureStatus.Failure, reported.single().status)
    }

    @Test
    fun traceSuspendReportsCancellationAndRethrowsSameError() {
        val reported = mutableListOf<BeezKitMeasureRecord>()
        val runtime = runtime(reporter = BeezKitMeasureReporter { reported += it })
        val expected = CancellationException("cancelled")

        val result = runSuspend {
            runtime.traceSuspend("cancel", emptyMap()) { throw expected }
        }

        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
        assertSame(expected, reported.single().error)
        assertEquals(BeezKitMeasureStatus.Cancelled, reported.single().status)
    }

    @Test
    fun disabledRuntimeOnlyExecutesBlock() {
        var clockReads = 0
        val runtime = BeezKitMeasureRuntime(
            clock = BeezKitMeasureClock {
                clockReads += 1
                nowNanos
            },
            initialConfig = BeezKitMeasureConfig(enabled = false, historyCapacity = 10),
        )

        val value = runtime.trace("", mapOf("" to "")) { 42 }

        assertEquals(42, value)
        assertEquals(0, clockReads)
        assertTrue(runtime.records().isEmpty())
        assertNull(runtime.start("", emptyMap()).end())
    }

    @Test
    fun historyEvictsOldestRecordAtCapacity() {
        val runtime = runtime(historyCapacity = 2)

        runtime.trace("first", emptyMap()) {}
        runtime.trace("second", emptyMap()) {}
        runtime.trace("third", emptyMap()) {}

        assertEquals(listOf("second", "third"), runtime.records().map { it.tag })
        runtime.clear()
        assertTrue(runtime.records().isEmpty())
    }

    @Test
    fun sameTagMarksEndInLifoOrder() {
        val runtime = runtime()

        assertTrue(runtime.markStart("load", null, emptyMap()))
        advanceBy(10)
        assertTrue(runtime.markStart("load", null, emptyMap()))
        advanceBy(20)

        assertEquals(20.milliseconds, runtime.markEnd("load", null)?.duration)
        advanceBy(30)
        assertEquals(60.milliseconds, runtime.markEnd("load", null)?.duration)
        assertNull(runtime.markEnd("load", null))
    }

    @Test
    fun activeMarkCountIsBounded() {
        val runtime = BeezKitMeasureRuntime(
            clock = BeezKitMeasureClock { nowNanos },
            initialConfig = BeezKitMeasureConfig(maxActiveMarks = 1),
        )

        assertTrue(runtime.markStart("first", null, emptyMap()))
        assertFalse(runtime.markStart("second", null, emptyMap()))
        assertEquals("first", runtime.markEnd("first", null)?.tag)
        assertTrue(runtime.markStart("second", null, emptyMap()))
    }

    @Test
    fun sameTagSpansHaveUniqueIds() {
        val runtime = runtime()

        val first = runtime.start("request", emptyMap())
        val second = runtime.start("request", emptyMap())

        assertTrue(first.id != second.id)
    }

    @Test
    fun concurrentEndCompletesSpanOnlyOnce() {
        val reported = ConcurrentLinkedQueue<BeezKitMeasureRecord>()
        val runtime = runtime(reporter = BeezKitMeasureReporter { reported += it })
        val span = runtime.start("shared", emptyMap())
        val start = CountDownLatch(1)
        val results = ConcurrentLinkedQueue<Boolean>()
        val threads = List(8) {
            Thread {
                start.await()
                results.add(span.end() != null)
            }.apply { start() }
        }

        start.countDown()
        threads.forEach { it.join() }

        assertEquals(1, results.count { it })
        assertEquals(1, reported.size)
    }

    @Test
    fun reporterExceptionDoesNotChangeTraceResultOrOtherReporters() {
        val reported = mutableListOf<BeezKitMeasureRecord>()
        val runtime = BeezKitMeasureRuntime(
            clock = BeezKitMeasureClock { nowNanos },
            initialConfig = BeezKitMeasureConfig(
                reporters = listOf(
                    BeezKitMeasureReporter { error("reporter failed") },
                    BeezKitMeasureReporter { reported += it },
                ),
            ),
        )

        assertEquals("value", runtime.trace("safe", emptyMap()) { "value" })
        assertEquals(1, reported.size)
    }

    @Test
    fun attributesAreCopiedAndValidated() {
        val reported = mutableListOf<BeezKitMeasureRecord>()
        val runtime = runtime(reporter = BeezKitMeasureReporter { reported += it })
        val attributes = mutableMapOf("source" to "initial")

        runtime.trace("copy", attributes) {
            attributes["source"] = "changed"
        }

        assertEquals("initial", reported.single().attributes["source"])
        try {
            runtime.trace("invalid", mapOf("key" to "x".repeat(257))) {}
            fail("Expected oversized attribute to fail")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    private fun runtime(
        historyCapacity: Int = 0,
        reporter: BeezKitMeasureReporter? = null,
    ): BeezKitMeasureRuntime = BeezKitMeasureRuntime(
        clock = BeezKitMeasureClock { nowNanos },
        initialConfig = BeezKitMeasureConfig(
            historyCapacity = historyCapacity,
            reporters = listOfNotNull(reporter),
        ),
    )

    private fun advanceBy(milliseconds: Long) {
        nowNanos += milliseconds * 1_000_000L
    }

    private fun <T> runSuspend(block: suspend () -> T): Result<T> {
        var completed: Result<T>? = null
        block.startCoroutine(
            object : Continuation<T> {
                override val context = EmptyCoroutineContext

                override fun resumeWith(result: Result<T>) {
                    completed = result
                }
            },
        )
        return checkNotNull(completed) { "Test block unexpectedly suspended" }
    }
}
