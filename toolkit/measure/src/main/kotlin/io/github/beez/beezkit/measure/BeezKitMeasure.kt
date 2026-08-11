package io.github.beez.beezkit.measure

import android.os.SystemClock
import java.util.concurrent.CancellationException
import kotlin.time.Duration.Companion.nanoseconds

public object BeezKitMeasure {
    private val runtime = BeezKitMeasureRuntime(
        clock = BeezKitMeasureClock(SystemClock::elapsedRealtimeNanos),
    )

    /** Replaces the process-wide configuration used by measurements started after this call. */
    public fun configure(block: BeezKitMeasureConfigBuilder.() -> Unit) {
        runtime.configure(block)
    }

    /** Measures [block] and returns its value without changing its exception behavior. */
    public fun <T> trace(
        tag: String,
        attributes: Map<String, String> = emptyMap(),
        block: () -> T,
    ): T = runtime.trace(tag, attributes, block)

    /** Measures a suspending [block] and preserves its return, failure, and cancellation behavior. */
    public suspend fun <T> traceSuspend(
        tag: String,
        attributes: Map<String, String> = emptyMap(),
        block: suspend () -> T,
    ): T = runtime.traceSuspend(tag, attributes, block)

    /** Starts an independently identified span. */
    public fun start(
        tag: String,
        attributes: Map<String, String> = emptyMap(),
    ): BeezKitMeasureSpan = runtime.start(tag, attributes)

    /** Starts a bounded, TAG-based convenience span. Prefer [start] for concurrent work. */
    public fun markStart(
        tag: String,
        key: String? = null,
        attributes: Map<String, String> = emptyMap(),
    ): Boolean = runtime.markStart(tag, key, attributes)

    /** Ends the most recently started span matching [tag] and [key]. */
    public fun markEnd(
        tag: String,
        key: String? = null,
    ): BeezKitMeasureRecord? = runtime.markEnd(tag, key)

    /** Returns an immutable oldest-to-newest snapshot of retained records. */
    public fun records(): List<BeezKitMeasureRecord> = runtime.records()

    public fun clear() {
        runtime.clear()
    }
}

internal fun interface BeezKitMeasureClock {
    fun nowNanos(): Long
}

internal class BeezKitMeasureRuntime(
    private val clock: BeezKitMeasureClock,
    initialConfig: BeezKitMeasureConfig = BeezKitMeasureConfig(),
) {
    private val lock = Any()
    private var state: RuntimeState = RuntimeState(initialConfig)
    private val marks: MutableMap<MarkKey, ArrayDeque<DefaultBeezKitMeasureSpan>> = mutableMapOf()
    private var activeMarkCount: Int = 0
    private var nextId: Long = 0L

    fun configure(block: BeezKitMeasureConfigBuilder.() -> Unit) {
        synchronized(lock) {
            val nextConfig = BeezKitMeasureConfigBuilder(state.config).apply(block).build()
            state = RuntimeState(nextConfig)
            marks.clear()
            activeMarkCount = 0
        }
    }

    fun <T> trace(
        tag: String,
        attributes: Map<String, String>,
        block: () -> T,
    ): T {
        val span = newSpanOrNull(tag, attributes) ?: return block()
        return try {
            block().also { span.complete(BeezKitMeasureStatus.Success, null) }
        } catch (cancellation: CancellationException) {
            span.complete(BeezKitMeasureStatus.Cancelled, cancellation)
            throw cancellation
        } catch (error: Throwable) {
            span.complete(BeezKitMeasureStatus.Failure, error)
            throw error
        }
    }

    suspend fun <T> traceSuspend(
        tag: String,
        attributes: Map<String, String>,
        block: suspend () -> T,
    ): T {
        val span = newSpanOrNull(tag, attributes) ?: return block()
        return try {
            block().also { span.complete(BeezKitMeasureStatus.Success, null) }
        } catch (cancellation: CancellationException) {
            span.complete(BeezKitMeasureStatus.Cancelled, cancellation)
            throw cancellation
        } catch (error: Throwable) {
            span.complete(BeezKitMeasureStatus.Failure, error)
            throw error
        }
    }

    fun start(tag: String, attributes: Map<String, String>): BeezKitMeasureSpan =
        newSpanOrNull(tag, attributes) ?: DisabledBeezKitMeasureSpan

    fun markStart(tag: String, key: String?, attributes: Map<String, String>): Boolean {
        val span = newSpanOrNull(tag, attributes) ?: return false
        synchronized(lock) {
            val limit = span.runtimeState.config.maxActiveMarks
            if (activeMarkCount >= limit) return false

            marks.getOrPut(MarkKey(tag, key)) { ArrayDeque() }.addLast(span)
            activeMarkCount += 1
            return true
        }
    }

    fun markEnd(tag: String, key: String?): BeezKitMeasureRecord? {
        val span = synchronized(lock) {
            val markKey = MarkKey(tag, key)
            val stack = marks[markKey] ?: return null
            val latest = stack.removeLast()
            activeMarkCount -= 1
            if (stack.isEmpty()) marks.remove(markKey)
            latest
        }
        return span.end()
    }

    fun records(): List<BeezKitMeasureRecord> = synchronized(lock) {
        state.history.toList()
    }

    fun clear() {
        synchronized(lock) {
            state.history.clear()
        }
    }

    private fun newSpanOrNull(
        tag: String,
        attributes: Map<String, String>,
    ): DefaultBeezKitMeasureSpan? {
        val runtimeState = synchronized(lock) {
            if (!state.config.enabled) return null
            state
        }
        val safeTag = validateTag(tag)
        val safeAttributes = validateAttributes(attributes)
        val id = synchronized(lock) {
            nextId += 1
            "measure-$nextId"
        }
        return DefaultBeezKitMeasureSpan(
            id = id,
            tag = safeTag,
            attributes = safeAttributes,
            startedNanos = clock.nowNanos(),
            clock = clock,
            runtimeState = runtimeState,
            onComplete = ::complete,
        )
    }

    private fun complete(runtimeState: RuntimeState, record: BeezKitMeasureRecord) {
        synchronized(lock) {
            runtimeState.history.add(record)
        }
        runtimeState.config.reporters.forEach { reporter ->
            try {
                reporter.report(record)
            } catch (_: Exception) {
                // Instrumentation must not change host application behavior.
            }
        }
    }

    private fun validateTag(tag: String): String {
        require(tag.isNotBlank()) { "tag must not be blank" }
        require(tag.length <= MAX_TAG_LENGTH) { "tag must be at most $MAX_TAG_LENGTH characters" }
        return tag
    }

    private fun validateAttributes(attributes: Map<String, String>): Map<String, String> {
        require(attributes.size <= MAX_ATTRIBUTE_COUNT) {
            "attributes must contain at most $MAX_ATTRIBUTE_COUNT entries"
        }
        attributes.forEach { (key, value) ->
            require(key.isNotBlank()) { "attribute keys must not be blank" }
            require(key.length <= MAX_ATTRIBUTE_KEY_LENGTH) {
                "attribute keys must be at most $MAX_ATTRIBUTE_KEY_LENGTH characters"
            }
            require(value.length <= MAX_ATTRIBUTE_VALUE_LENGTH) {
                "attribute values must be at most $MAX_ATTRIBUTE_VALUE_LENGTH characters"
            }
        }
        return attributes.toMap()
    }

    internal class RuntimeState(
        val config: BeezKitMeasureConfig,
    ) {
        val history = BoundedRecordBuffer(config.historyCapacity)
    }

    private data class MarkKey(val tag: String, val key: String?)

    private companion object {
        const val MAX_TAG_LENGTH = 128
        const val MAX_ATTRIBUTE_COUNT = 16
        const val MAX_ATTRIBUTE_KEY_LENGTH = 64
        const val MAX_ATTRIBUTE_VALUE_LENGTH = 256
    }
}

internal class DefaultBeezKitMeasureSpan(
    override val id: String,
    override val tag: String,
    private val attributes: Map<String, String>,
    private val startedNanos: Long,
    private val clock: BeezKitMeasureClock,
    internal val runtimeState: BeezKitMeasureRuntime.RuntimeState,
    private val onComplete: (BeezKitMeasureRuntime.RuntimeState, BeezKitMeasureRecord) -> Unit,
) : BeezKitMeasureSpan {
    private val lock = Any()
    private var completed: Boolean = false

    override fun end(): BeezKitMeasureRecord? = complete(BeezKitMeasureStatus.Success, null)

    fun complete(status: BeezKitMeasureStatus, error: Throwable?): BeezKitMeasureRecord? {
        val record = synchronized(lock) {
            if (completed) return null
            completed = true
            val elapsedNanos = (clock.nowNanos() - startedNanos).coerceAtLeast(0L)
            BeezKitMeasureRecord(
                id = id,
                tag = tag,
                duration = elapsedNanos.nanoseconds,
                status = status,
                attributes = attributes,
                error = error,
            )
        }
        onComplete(runtimeState, record)
        return record
    }
}

internal object DisabledBeezKitMeasureSpan : BeezKitMeasureSpan {
    override val id: String = ""
    override val tag: String = ""
    override fun end(): BeezKitMeasureRecord? = null
}

internal class BoundedRecordBuffer(
    private val capacity: Int,
) {
    private val records = ArrayDeque<BeezKitMeasureRecord>(capacity.coerceAtLeast(1))

    fun add(record: BeezKitMeasureRecord) {
        if (capacity == 0) return
        if (records.size == capacity) records.removeFirst()
        records.addLast(record)
    }

    fun clear() {
        records.clear()
    }

    fun toList(): List<BeezKitMeasureRecord> = records.toList()
}
