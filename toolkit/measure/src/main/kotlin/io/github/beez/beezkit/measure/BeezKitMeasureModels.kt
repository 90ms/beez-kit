package io.github.beez.beezkit.measure

import kotlin.time.Duration

public enum class BeezKitMeasureStatus {
    Success,
    Failure,
    Cancelled,
}

public data class BeezKitMeasureRecord(
    public val id: String,
    public val tag: String,
    public val duration: Duration,
    public val status: BeezKitMeasureStatus,
    public val attributes: Map<String, String>,
    public val error: Throwable?,
)

public fun interface BeezKitMeasureReporter {
    public fun report(record: BeezKitMeasureRecord)
}

public interface BeezKitMeasureSpan : AutoCloseable {
    public val id: String
    public val tag: String

    /** Completes this span once. Later calls return `null`. */
    public fun end(): BeezKitMeasureRecord?

    override fun close() {
        end()
    }
}

public data class BeezKitMeasureConfig(
    public val enabled: Boolean = true,
    public val historyCapacity: Int = 0,
    public val maxActiveMarks: Int = 1_024,
    public val reporters: List<BeezKitMeasureReporter> = emptyList(),
) {
    init {
        require(historyCapacity >= 0) { "historyCapacity must not be negative" }
        require(maxActiveMarks >= 0) { "maxActiveMarks must not be negative" }
    }
}

public class BeezKitMeasureConfigBuilder internal constructor(
    config: BeezKitMeasureConfig,
) {
    public var enabled: Boolean = config.enabled
    public var historyCapacity: Int = config.historyCapacity
    public var maxActiveMarks: Int = config.maxActiveMarks

    private val reporters: MutableList<BeezKitMeasureReporter> = config.reporters.toMutableList()

    public fun reporter(reporter: BeezKitMeasureReporter) {
        reporters += reporter
    }

    public fun clearReporters() {
        reporters.clear()
    }

    internal fun build(): BeezKitMeasureConfig = BeezKitMeasureConfig(
        enabled = enabled,
        historyCapacity = historyCapacity,
        maxActiveMarks = maxActiveMarks,
        reporters = reporters.toList(),
    )
}
