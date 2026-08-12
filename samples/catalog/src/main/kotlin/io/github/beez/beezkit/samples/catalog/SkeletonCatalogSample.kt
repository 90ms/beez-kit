package io.github.beez.beezkit.samples.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.beez.beezkit.skeleton.BeezKitSkeletonAnimation
import io.github.beez.beezkit.skeleton.BeezKitSkeletonContainer
import io.github.beez.beezkit.skeleton.BeezKitSkeletonDefaults
import io.github.beez.beezkit.skeleton.BeezKitSkeletonDirection
import io.github.beez.beezkit.skeleton.BeezKitSkeletonScope
import io.github.beez.beezkit.skeleton.BeezKitSkeletonTransition
import io.github.beez.beezkit.skeleton.bkSkeleton
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun SkeletonCatalogSample() {
    var loading by remember { mutableStateOf(true) }
    var animationIndex by remember { mutableIntStateOf(0) }
    var fast by remember { mutableStateOf(false) }
    var warmColors by remember { mutableStateOf(false) }
    var wideBand by remember { mutableStateOf(false) }
    var directionIndex by remember { mutableIntStateOf(0) }
    val animationNames = listOf("Shimmer", "Pulse", "Static")
    val directions = BeezKitSkeletonDirection.entries
    val duration = if (fast) 600.milliseconds else 1_200.milliseconds
    val animation = when (animationIndex) {
        0 -> BeezKitSkeletonAnimation.Shimmer(
            duration = duration,
            widthFraction = if (wideBand) 0.65f else 0.35f,
            direction = directions[directionIndex],
        )
        1 -> BeezKitSkeletonAnimation.Pulse(duration = duration)
        else -> BeezKitSkeletonAnimation.Static
    }
    val colors = BeezKitSkeletonDefaults.colors(
        base = if (warmColors) Color(0xFFE8D9CF) else MaterialTheme.colorScheme.surfaceVariant,
        highlight = if (warmColors) Color(0xFFFFF4EC) else MaterialTheme.colorScheme.surface,
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "컴포넌트와 화면 단위 로딩 상태, 공유 애니메이션을 확인하세요.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { loading = !loading }) {
                Text(if (loading) "콘텐츠 표시" else "로딩 표시")
            }
            Button(onClick = { animationIndex = (animationIndex + 1) % animationNames.size }) {
                Text(animationNames[animationIndex])
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { fast = !fast }) {
                Text(if (fast) "빠름" else "보통 속도")
            }
            Button(onClick = { warmColors = !warmColors }) {
                Text(if (warmColors) "웜 컬러" else "테마 컬러")
            }
        }
        if (animationIndex == 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { directionIndex = (directionIndex + 1) % directions.size }) {
                    Text(directions[directionIndex].name)
                }
                Button(onClick = { wideBand = !wideBand }) {
                    Text(if (wideBand) "넓은 밴드" else "기본 밴드")
                }
            }
        }

        Text("단일 Modifier", style = MaterialTheme.typography.titleSmall)
        Text(
            text = if (loading) "" else "BeezKit Skeleton",
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .bkSkeleton(
                    visible = loading,
                    colors = colors,
                    animation = animation,
                )
                .padding(8.dp),
        )

        Text("공유 Scope", style = MaterialTheme.typography.titleSmall)
        BeezKitSkeletonScope(
            visible = loading,
            style = BeezKitSkeletonDefaults.style(
                colors = colors,
                animation = animation,
            ),
            minimumVisibleDuration = 300.milliseconds,
            loadingDescription = "프로필을 불러오는 중",
        ) {
            ProfileSkeleton()
        }

        Text("화면 Container", style = MaterialTheme.typography.titleSmall)
        BeezKitSkeletonContainer(
            loading = loading,
            style = BeezKitSkeletonDefaults.style(
                colors = colors,
                animation = animation,
            ),
            transition = BeezKitSkeletonTransition.Crossfade(),
            skeleton = { ProfileSkeleton() },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("김비즈", style = MaterialTheme.typography.titleMedium)
                Text("Skeleton 콘텐츠가 준비되었습니다.")
            }
        }
    }
}

@Composable
private fun BeezKitSkeletonScope.ProfileSkeleton() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(56.dp)
                .bkSkeleton(shape = CircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .bkSkeleton(),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .bkSkeleton(shape = RoundedCornerShape(4.dp)),
            )
        }
    }
}
