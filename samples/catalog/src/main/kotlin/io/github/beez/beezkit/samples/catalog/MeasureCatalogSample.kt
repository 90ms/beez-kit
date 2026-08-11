package io.github.beez.beezkit.samples.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import io.github.beez.beezkit.measure.BeezKitMeasure
import io.github.beez.beezkit.measure.BeezKitMeasureRecord
import io.github.beez.beezkit.measure.BeezKitMeasureSpan

@Composable
internal fun MeasureCatalogSample() {
    var records by remember { mutableStateOf(emptyList<BeezKitMeasureRecord>()) }
    var activeSpan by remember { mutableStateOf<BeezKitMeasureSpan?>(null) }
    var message by remember { mutableStateOf("측정을 실행해 보세요.") }

    DisposableEffect(Unit) {
        BeezKitMeasure.configure {
            enabled = true
            historyCapacity = 10
            maxActiveMarks = 16
            clearReporters()
        }
        BeezKitMeasure.clear()
        onDispose {
            BeezKitMeasure.clear()
            BeezKitMeasure.configure {
                enabled = true
                historyCapacity = 0
                maxActiveMarks = 1_024
                clearReporters()
            }
        }
    }

    fun refreshRecords() {
        records = BeezKitMeasure.records()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "반환값과 예외 흐름을 유지하면서 최근 10개의 측정 결과만 보관합니다.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val result = BeezKitMeasure.trace(
                    tag = "catalog-calculation",
                    attributes = mapOf("source" to "catalog"),
                ) {
                    (1..10_000).sum()
                }
                message = "블록 반환값: $result"
                refreshRecords()
            },
        ) {
            Text("블록 측정")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                try {
                    BeezKitMeasure.trace("catalog-failure") {
                        error("샘플 오류")
                    }
                } catch (error: IllegalStateException) {
                    message = "동일한 예외 재전파: ${error.message}"
                }
                refreshRecords()
            },
        ) {
            Text("실패 측정")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = activeSpan == null,
                onClick = {
                    activeSpan = BeezKitMeasure.start("catalog-manual")
                    message = "수동 span 시작"
                },
            ) {
                Text("Span 시작")
            }
            Button(
                enabled = activeSpan != null,
                onClick = {
                    activeSpan?.end()
                    activeSpan = null
                    message = "수동 span 종료"
                    refreshRecords()
                },
            ) {
                Text("Span 종료")
            }
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                BeezKitMeasure.markStart("catalog-nested")
                BeezKitMeasure.markStart("catalog-nested")
                BeezKitMeasure.markEnd("catalog-nested")
                BeezKitMeasure.markEnd("catalog-nested")
                message = "동일 TAG 중첩 측정 2개 완료"
                refreshRecords()
            },
        ) {
            Text("동일 TAG 중첩 측정")
        }

        Text(message, color = MaterialTheme.colorScheme.primary)
        Text("최근 결과 (${records.size}/10)", style = MaterialTheme.typography.titleSmall)
        records.asReversed().forEach { record ->
            Text(
                text = "${record.tag} · ${record.status} · ${record.duration.inWholeMicroseconds}µs",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
