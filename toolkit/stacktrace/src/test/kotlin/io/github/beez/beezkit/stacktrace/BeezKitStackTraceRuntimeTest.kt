package io.github.beez.beezkit.stacktrace

import java.util.concurrent.CountDownLatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BeezKitStackTraceRuntimeTest {
    @Test
    fun filtersInternalAndFrameworkFramesByDefault() {
        val runtime = runtime(
            frame("io.github.beez.beezkit.stacktrace.Internal", "collect"),
            frame("java.lang.Thread", "getStackTrace"),
            frame("android.view.View", "performClick"),
            frame("com.example.UserViewModel", "refresh", "UserViewModel.kt", 42),
        )

        val record = log(runtime)

        assertEquals(1, record?.frames?.size)
        assertEquals("com.example.UserViewModel", record?.frames?.single()?.className)
        assertEquals(42, record?.frames?.single()?.lineNumber)
    }

    @Test
    fun canIncludeFrameworkFrames() {
        val runtime = runtime(
            frame("android.view.View", "performClick"),
            frame("com.example.Screen", "onClick"),
        )

        val record = log(runtime, includeFrameworkFrames = true)

        assertEquals(
            listOf("android.view.View", "com.example.Screen"),
            record?.frames?.map { it.className },
        )
    }

    @Test
    fun appliesExcludedPackagesThenSkipAndLimit() {
        val runtime = runtime(
            frame("com.example.logging.Wrapper", "log"),
            frame("com.example.First", "call"),
            frame("com.example.Second", "call"),
            frame("com.example.Third", "call"),
            config = BeezKitStackTraceConfig(
                excludedPackagePrefixes = setOf("com.example.logging"),
            ),
        )

        val record = log(runtime, maxFrames = 1, skipFrames = 1)

        assertEquals("com.example.Second", record?.frames?.single()?.className)
    }

    @Test
    fun disabledModeDoesNotEvaluateValueOrCollectStack() {
        var providerCalls = 0
        var collectorCalls = 0
        val runtime = BeezKitStackTraceRuntime(
            collector = BeezKitStackTraceCollector {
                collectorCalls += 1
                emptyList()
            },
            initialConfig = BeezKitStackTraceConfig(enabled = false),
        )

        val record = runtime.log(
            tag = null,
            maxFrames = null,
            skipFrames = -1,
            includeFrameworkFrames = null,
            valueProvider = {
                providerCalls += 1
                "value"
            },
            providerFailureLabel = "value-provider-error",
        )

        assertNull(record)
        assertEquals(0, providerCalls)
        assertEquals(0, collectorCalls)
    }

    @Test
    fun isolatesLazyProviderFailureWithoutMessage() {
        val runtime = runtime(frame("com.example.Host", "call"))

        val record = runtime.log(
            tag = "failure",
            maxFrames = null,
            skipFrames = 0,
            includeFrameworkFrames = null,
            valueProvider = { throw IllegalStateException("secret") },
            providerFailureLabel = "value-provider-error",
        )

        assertEquals("<value-provider-error: IllegalStateException>", record?.value)
        assertFalse(record?.value.orEmpty().contains("secret"))
    }

    @Test
    fun isolatesImmediateFormatterFailure() {
        val runtime = runtime(frame("com.example.Host", "call"))

        val record = runtime.log(
            tag = null,
            maxFrames = null,
            skipFrames = 0,
            includeFrameworkFrames = null,
            valueProvider = { error("cannot format") },
            providerFailureLabel = "value-format-error",
        )

        assertEquals("<value-format-error: IllegalStateException>", record?.value)
    }

    @Test
    fun truncatesValueToConfiguredLength() {
        val runtime = runtime(
            frame("com.example.Host", "call"),
            config = BeezKitStackTraceConfig(maxValueLength = 16),
        )

        val record = log(runtime, value = "abcdefghijklmnopqrstuvwxyz")

        assertEquals(16, record?.value?.length)
        assertTrue(record?.value.orEmpty().endsWith("…[truncated]"))
    }

    @Test
    fun historyEvictsOldestAtCapacityAndCanClear() {
        val runtime = runtime(
            frame("com.example.Host", "call"),
            config = BeezKitStackTraceConfig(historyCapacity = 2),
        )

        log(runtime, value = "first")
        log(runtime, value = "second")
        log(runtime, value = "third")

        assertEquals(listOf("second", "third"), runtime.records().map { it.value })
        runtime.clear()
        assertTrue(runtime.records().isEmpty())
    }

    @Test
    fun reporterFailureDoesNotAffectCallOrNextReporter() {
        val reported = mutableListOf<BeezKitStackTraceRecord>()
        val runtime = runtime(
            frame("com.example.Host", "call"),
            config = BeezKitStackTraceConfig(
                reporters = listOf(
                    BeezKitStackTraceReporter { error("reporter failed") },
                    BeezKitStackTraceReporter { reported += it },
                ),
            ),
        )

        val record = log(runtime)

        assertEquals(record, reported.single())
    }

    @Test
    fun frameAndHistorySnapshotsCannotBeMutated() {
        val runtime = runtime(
            frame("com.example.Host", "call"),
            config = BeezKitStackTraceConfig(historyCapacity = 1),
        )
        val record = log(runtime) ?: error("Expected record")

        try {
            @Suppress("UNCHECKED_CAST")
            (record.frames as MutableList<BeezKitStackFrame>).clear()
            fail("Expected immutable frames")
        } catch (_: UnsupportedOperationException) {
            // Expected.
        }
        try {
            @Suppress("UNCHECKED_CAST")
            (runtime.records() as MutableList<BeezKitStackTraceRecord>).clear()
            fail("Expected immutable history")
        } catch (_: UnsupportedOperationException) {
            // Expected.
        }
    }

    @Test
    fun concurrentLoggingKeepsHistoryBounded() {
        val runtime = runtime(
            frame("com.example.Host", "call"),
            config = BeezKitStackTraceConfig(historyCapacity = 10),
        )
        val start = CountDownLatch(1)
        val threads = List(40) { index ->
            Thread {
                start.await()
                log(runtime, value = index.toString())
            }.apply { start() }
        }

        start.countDown()
        threads.forEach { it.join() }

        assertEquals(10, runtime.records().size)
    }

    @Test
    fun rejectsInvalidEnabledOptions() {
        val runtime = runtime(frame("com.example.Host", "call"))

        try {
            log(runtime, maxFrames = 0)
            fail("Expected maxFrames validation")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
        try {
            log(runtime, skipFrames = -1)
            fail("Expected skipFrames validation")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    private fun runtime(
        vararg frames: StackTraceElement,
        config: BeezKitStackTraceConfig = BeezKitStackTraceConfig(),
    ): BeezKitStackTraceRuntime = BeezKitStackTraceRuntime(
        collector = BeezKitStackTraceCollector { frames.toList() },
        initialConfig = config,
    )

    private fun log(
        runtime: BeezKitStackTraceRuntime,
        value: String = "value",
        maxFrames: Int? = null,
        skipFrames: Int = 0,
        includeFrameworkFrames: Boolean? = null,
    ): BeezKitStackTraceRecord? = runtime.log(
        tag = "test",
        maxFrames = maxFrames,
        skipFrames = skipFrames,
        includeFrameworkFrames = includeFrameworkFrames,
        valueProvider = { value },
        providerFailureLabel = "value-provider-error",
    )

    private fun frame(
        className: String,
        methodName: String,
        fileName: String? = null,
        lineNumber: Int = -1,
    ): StackTraceElement = StackTraceElement(className, methodName, fileName, lineNumber)
}
