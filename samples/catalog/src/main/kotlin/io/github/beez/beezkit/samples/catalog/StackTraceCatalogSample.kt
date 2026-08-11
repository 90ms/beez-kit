package io.github.beez.beezkit.samples.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.beez.beezkit.stacktrace.BeezKitStackTrace
import io.github.beez.beezkit.stacktrace.BeezKitStackTraceRecord

@Composable
internal fun StackTraceCatalogSample() {
    var records by remember { mutableStateOf(emptyList<BeezKitStackTraceRecord>()) }
    var message by remember { mutableStateOf("호출 경로를 수집해 보세요.") }

    DisposableEffect(Unit) {
        configureCatalogStackTrace(enabled = true)
        BeezKitStackTrace.clear()
        onDispose {
            BeezKitStackTrace.clear()
            BeezKitStackTrace.configure {
                enabled = true
                historyCapacity = 0
                defaultMaxFrames = 8
                maxValueLength = 512
                includeFrameworkFrames = false
                clearExcludedPackages()
                clearReporters()
            }
        }
    }

    fun refresh() {
        records = BeezKitStackTrace.records()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "기본적으로 BeezKit과 framework 프레임을 제외하고 최근 10개만 보관합니다.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                BeezKitStackTrace.log(
                    value = "user-123",
                    tag = "catalog-basic",
                )
                message = "기본 호출 경로를 기록했습니다."
                refresh()
            },
        ) {
            Text("기본 한 줄 기록")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                catalogWrappedStackTrace()
                message = "Lazy value와 skipFrames=1을 적용했습니다."
                refresh()
            },
        ) {
            Text("Wrapper 건너뛰기")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                BeezKitStackTrace.log(
                    value = "framework-visible",
                    tag = "catalog-framework",
                    maxFrames = 12,
                    includeFrameworkFrames = true,
                )
                message = "Framework 프레임을 포함했습니다."
                refresh()
            },
        ) {
            Text("Framework 프레임 포함")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                var lazyCalls = 0
                configureCatalogStackTrace(enabled = false)
                BeezKitStackTrace.log(tag = "catalog-disabled") {
                    lazyCalls += 1
                    "실행되면 안 됨"
                }
                configureCatalogStackTrace(enabled = true)
                message = "비활성 상태 lazy 실행 횟수: $lazyCalls"
                refresh()
            },
        ) {
            Text("비활성 Fast Path 확인")
        }

        Text(message, color = MaterialTheme.colorScheme.primary)
        Text("최근 결과 (${records.size}/10)", style = MaterialTheme.typography.titleSmall)
        records.asReversed().forEach { record ->
            val first = record.frames.firstOrNull()
            Text(
                text = buildString {
                    append(record.tag ?: "no-tag")
                    append(" · ")
                    append(record.value)
                    if (first != null) {
                        append("\n↳ ")
                        append(first.className.substringAfterLast('.'))
                        append('.')
                        append(first.methodName)
                        first.lineNumber?.let { append(":$it") }
                    }
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun catalogWrappedStackTrace() {
    BeezKitStackTrace.log(
        tag = "catalog-wrapper",
        skipFrames = 1,
    ) {
        "lazy-state=ready"
    }
}

private fun configureCatalogStackTrace(enabled: Boolean) {
    BeezKitStackTrace.configure {
        this.enabled = enabled
        historyCapacity = 10
        defaultMaxFrames = 8
        maxValueLength = 128
        includeFrameworkFrames = false
        clearExcludedPackages()
        clearReporters()
    }
}
