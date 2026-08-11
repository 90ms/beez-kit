package io.github.beez.beezkit.samples.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.beez.beezkit.throttle.bkThrottledClickable
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun ThrottleCatalogSample() {
    var defaultCount by remember { mutableIntStateOf(0) }
    var customCount by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "아래 영역을 빠르게 여러 번 눌러 leading-edge 제한을 확인하세요.",
            style = MaterialTheme.typography.bodyMedium,
        )
        ThrottleTarget(
            title = "기본 간격 500ms",
            count = defaultCount,
            modifier = Modifier.bkThrottledClickable(
                role = Role.Button,
                onClickLabel = "기본 쓰로틀 클릭",
            ) {
                defaultCount += 1
            },
        )
        ThrottleTarget(
            title = "사용자 지정 간격 2초",
            count = customCount,
            modifier = Modifier.bkThrottledClickable(
                interval = 2.seconds,
                role = Role.Button,
                onClickLabel = "사용자 지정 쓰로틀 클릭",
            ) {
                customCount += 1
            },
        )
    }
}

@Composable
private fun ThrottleTarget(
    title: String,
    count: Int,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text("허용된 클릭: ${count}회")
        }
    }
}
