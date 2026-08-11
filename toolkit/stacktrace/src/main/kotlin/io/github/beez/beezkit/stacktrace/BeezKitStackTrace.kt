package io.github.beez.beezkit.stacktrace

import java.util.Collections

public object BeezKitStackTrace {
    private val runtime = BeezKitStackTraceRuntime(
        collector = BeezKitStackTraceCollector {
            Thread.currentThread().stackTrace.toList()
        },
    )

    public fun configure(block: BeezKitStackTraceConfigBuilder.() -> Unit) {
        runtime.configure(block)
    }

    /** Converts [value] immediately and captures the current relevant call path. */
    public fun log(
        value: Any?,
        tag: String? = null,
        maxFrames: Int? = null,
        skipFrames: Int = 0,
        includeFrameworkFrames: Boolean? = null,
    ): BeezKitStackTraceRecord? = runtime.log(
        tag = tag,
        maxFrames = maxFrames,
        skipFrames = skipFrames,
        includeFrameworkFrames = includeFrameworkFrames,
        valueProvider = { formatValue(value) },
        providerFailureLabel = "value-format-error",
    )

    /** Does not evaluate [value] when collection is disabled. Provider exceptions are isolated. */
    public fun log(
        tag: String? = null,
        maxFrames: Int? = null,
        skipFrames: Int = 0,
        includeFrameworkFrames: Boolean? = null,
        value: () -> String,
    ): BeezKitStackTraceRecord? = runtime.log(
        tag = tag,
        maxFrames = maxFrames,
        skipFrames = skipFrames,
        includeFrameworkFrames = includeFrameworkFrames,
        valueProvider = value,
        providerFailureLabel = "value-provider-error",
    )

    public fun records(): List<BeezKitStackTraceRecord> = runtime.records()

    public fun clear() {
        runtime.clear()
    }

    private fun formatValue(value: Any?): String = value?.toString() ?: "null"
}

internal fun interface BeezKitStackTraceCollector {
    fun collect(): List<StackTraceElement>
}

internal class BeezKitStackTraceRuntime(
    private val collector: BeezKitStackTraceCollector,
    initialConfig: BeezKitStackTraceConfig = BeezKitStackTraceConfig(),
) {
    private val lock = Any()
    private var state = RuntimeState(initialConfig.snapshot())

    fun configure(block: BeezKitStackTraceConfigBuilder.() -> Unit) {
        synchronized(lock) {
            val next = BeezKitStackTraceConfigBuilder(state.config).apply(block).build().snapshot()
            state = RuntimeState(next)
        }
    }

    fun log(
        tag: String?,
        maxFrames: Int?,
        skipFrames: Int,
        includeFrameworkFrames: Boolean?,
        valueProvider: () -> String,
        providerFailureLabel: String,
    ): BeezKitStackTraceRecord? {
        val runtimeState = synchronized(lock) {
            if (!state.config.enabled) return null
            state
        }
        val safeTag = validateTag(tag)
        val frameLimit = maxFrames ?: runtimeState.config.defaultMaxFrames
        require(frameLimit in 1..BEEZ_KIT_STACK_TRACE_MAX_FRAMES) {
            "maxFrames must be between 1 and $BEEZ_KIT_STACK_TRACE_MAX_FRAMES"
        }
        require(skipFrames >= 0) { "skipFrames must not be negative" }

        val rawValue = try {
            valueProvider()
        } catch (error: Exception) {
            "<$providerFailureLabel: ${error.javaClass.simpleName.ifBlank { "Exception" }}>"
        }
        val value = truncate(rawValue, runtimeState.config.maxValueLength)
        val includeFramework = includeFrameworkFrames
            ?: runtimeState.config.includeFrameworkFrames
        val frames = collector.collect()
            .asSequence()
            .filterNot(::isAlwaysExcluded)
            .filterNot { frame ->
                runtimeState.config.excludedPackagePrefixes.any { prefix ->
                    frame.className.startsWith(prefix)
                }
            }
            .filter { includeFramework || !isFrameworkFrame(it) }
            .drop(skipFrames)
            .take(frameLimit)
            .map(::toPublicFrame)
            .toList()
            .let { Collections.unmodifiableList(it) }
        val record = BeezKitStackTraceRecord(
            tag = safeTag,
            value = value,
            frames = frames,
        )

        synchronized(lock) {
            runtimeState.history.add(record)
        }
        runtimeState.config.reporters.forEach { reporter ->
            try {
                reporter.report(record)
            } catch (_: Exception) {
                // Diagnostics must not change host application behavior.
            }
        }
        return record
    }

    fun records(): List<BeezKitStackTraceRecord> = synchronized(lock) {
        Collections.unmodifiableList(state.history.toList())
    }

    fun clear() {
        synchronized(lock) {
            state.history.clear()
        }
    }

    private fun validateTag(tag: String?): String? {
        if (tag == null) return null
        require(tag.isNotBlank()) { "tag must not be blank" }
        require(tag.length <= 64) { "tag must be at most 64 characters" }
        return tag
    }

    private fun isAlwaysExcluded(frame: StackTraceElement): Boolean =
        frame.className.startsWith(STACKTRACE_PACKAGE) ||
            frame.className == "java.lang.Thread" ||
            REFLECTION_PREFIXES.any { prefix -> frame.className.startsWith(prefix) }

    private fun isFrameworkFrame(frame: StackTraceElement): Boolean =
        FRAMEWORK_PREFIXES.any { prefix -> frame.className.startsWith(prefix) }

    private fun toPublicFrame(frame: StackTraceElement): BeezKitStackFrame = BeezKitStackFrame(
        className = frame.className,
        methodName = frame.methodName,
        fileName = frame.fileName,
        lineNumber = frame.lineNumber.takeIf { it > 0 },
    )

    private fun truncate(value: String, maxLength: Int): String {
        if (value.length <= maxLength) return value
        if (maxLength <= TRUNCATED_SUFFIX.length) return TRUNCATED_SUFFIX.take(maxLength)
        return value.take(maxLength - TRUNCATED_SUFFIX.length) + TRUNCATED_SUFFIX
    }

    internal class RuntimeState(val config: BeezKitStackTraceConfig) {
        val history = BoundedStackTraceBuffer(config.historyCapacity)
    }

    private companion object {
        const val STACKTRACE_PACKAGE = "io.github.beez.beezkit.stacktrace."
        const val TRUNCATED_SUFFIX = "…[truncated]"

        val REFLECTION_PREFIXES = listOf(
            "java.lang.reflect.",
            "jdk.internal.reflect.",
            "sun.reflect.",
        )
        val FRAMEWORK_PREFIXES = listOf(
            "android.",
            "androidx.compose.",
            "kotlin.coroutines.",
            "kotlinx.coroutines.",
        )
    }
}

internal class BoundedStackTraceBuffer(
    private val capacity: Int,
) {
    private val records = ArrayDeque<BeezKitStackTraceRecord>(capacity.coerceAtLeast(1))

    fun add(record: BeezKitStackTraceRecord) {
        if (capacity == 0) return
        if (records.size == capacity) records.removeFirst()
        records.addLast(record)
    }

    fun clear() {
        records.clear()
    }

    fun toList(): List<BeezKitStackTraceRecord> = records.toList()
}

private fun BeezKitStackTraceConfig.snapshot(): BeezKitStackTraceConfig = copy(
    excludedPackagePrefixes = Collections.unmodifiableSet(excludedPackagePrefixes.toSet()),
    reporters = Collections.unmodifiableList(reporters.toList()),
)
