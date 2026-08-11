package io.github.beez.beezkit.stacktrace

public data class BeezKitStackFrame(
    public val className: String,
    public val methodName: String,
    public val fileName: String?,
    public val lineNumber: Int?,
)

public data class BeezKitStackTraceRecord(
    public val tag: String?,
    public val value: String,
    public val frames: List<BeezKitStackFrame>,
)

public fun interface BeezKitStackTraceReporter {
    public fun report(record: BeezKitStackTraceRecord)
}

public data class BeezKitStackTraceConfig(
    public val enabled: Boolean = true,
    public val historyCapacity: Int = 0,
    public val defaultMaxFrames: Int = 8,
    public val maxValueLength: Int = 512,
    public val includeFrameworkFrames: Boolean = false,
    public val excludedPackagePrefixes: Set<String> = emptySet(),
    public val reporters: List<BeezKitStackTraceReporter> = emptyList(),
) {
    init {
        require(historyCapacity in 0..BEEZ_KIT_STACK_TRACE_MAX_HISTORY) {
            "historyCapacity must be between 0 and $BEEZ_KIT_STACK_TRACE_MAX_HISTORY"
        }
        require(defaultMaxFrames in 1..BEEZ_KIT_STACK_TRACE_MAX_FRAMES) {
            "defaultMaxFrames must be between 1 and $BEEZ_KIT_STACK_TRACE_MAX_FRAMES"
        }
        require(maxValueLength in 1..BEEZ_KIT_STACK_TRACE_MAX_VALUE_LENGTH) {
            "maxValueLength must be between 1 and $BEEZ_KIT_STACK_TRACE_MAX_VALUE_LENGTH"
        }
        require(excludedPackagePrefixes.size <= 32) {
            "excludedPackagePrefixes must contain at most 32 entries"
        }
        excludedPackagePrefixes.forEach(::validatePackagePrefix)
        require(reporters.size <= 32) { "reporters must contain at most 32 entries" }
    }
}

public class BeezKitStackTraceConfigBuilder internal constructor(
    config: BeezKitStackTraceConfig,
) {
    public var enabled: Boolean = config.enabled
    public var historyCapacity: Int = config.historyCapacity
    public var defaultMaxFrames: Int = config.defaultMaxFrames
    public var maxValueLength: Int = config.maxValueLength
    public var includeFrameworkFrames: Boolean = config.includeFrameworkFrames

    private val excludedPackagePrefixes = config.excludedPackagePrefixes.toMutableSet()
    private val reporters = config.reporters.toMutableList()

    public fun excludePackage(prefix: String) {
        validatePackagePrefix(prefix)
        require(excludedPackagePrefixes.size < 32 || prefix in excludedPackagePrefixes) {
            "excludedPackagePrefixes must contain at most 32 entries"
        }
        excludedPackagePrefixes += prefix
    }

    public fun clearExcludedPackages() {
        excludedPackagePrefixes.clear()
    }

    public fun reporter(reporter: BeezKitStackTraceReporter) {
        require(reporters.size < 32) { "reporters must contain at most 32 entries" }
        reporters += reporter
    }

    public fun clearReporters() {
        reporters.clear()
    }

    internal fun build(): BeezKitStackTraceConfig = BeezKitStackTraceConfig(
        enabled = enabled,
        historyCapacity = historyCapacity,
        defaultMaxFrames = defaultMaxFrames,
        maxValueLength = maxValueLength,
        includeFrameworkFrames = includeFrameworkFrames,
        excludedPackagePrefixes = excludedPackagePrefixes.toSet(),
        reporters = reporters.toList(),
    )
}

private fun validatePackagePrefix(prefix: String) {
    require(prefix.isNotBlank()) { "package prefix must not be blank" }
    require(prefix.length <= 200) { "package prefix must be at most 200 characters" }
}

internal const val BEEZ_KIT_STACK_TRACE_MAX_HISTORY: Int = 10_000
internal const val BEEZ_KIT_STACK_TRACE_MAX_FRAMES: Int = 256
internal const val BEEZ_KIT_STACK_TRACE_MAX_VALUE_LENGTH: Int = 16_384
