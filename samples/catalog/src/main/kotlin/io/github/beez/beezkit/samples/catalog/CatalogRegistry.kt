package io.github.beez.beezkit.samples.catalog

import androidx.compose.material3.Text

internal val beezKitCatalogEntries: List<BeezKitCatalogEntry> = listOf(
    plannedEntry(
        id = "throttle",
        modulePath = ":toolkit:throttle",
        title = "Throttle",
        description = "Compose에서 빠르게 반복되는 입력을 제한하는 Modifier를 제공합니다.",
        category = BeezKitCatalogCategory.Toolkit,
    ),
    plannedEntry(
        id = "stacktrace",
        modulePath = ":toolkit:stacktrace",
        title = "Stack Trace",
        description = "값과 함께 호출 지점까지의 유효한 스택 프레임을 기록합니다.",
        category = BeezKitCatalogCategory.Toolkit,
    ),
    plannedEntry(
        id = "measure",
        modulePath = ":toolkit:measure",
        title = "Measure",
        description = "코드 구간의 실행 시간을 안전하게 측정하고 결과를 수집합니다.",
        category = BeezKitCatalogCategory.Toolkit,
    ),
    plannedEntry(
        id = "inspector-core",
        modulePath = ":toolkit:inspector:core",
        title = "Inspector Core",
        description = "진단 데이터를 탐색하는 접이식 인앱 플로팅 패널을 제공합니다.",
        category = BeezKitCatalogCategory.Debug,
    ),
    plannedEntry(
        id = "inspector-network",
        modulePath = ":toolkit:inspector:network",
        title = "Inspector Network",
        description = "호스트 앱이 선택한 네트워크 요청과 응답 정보를 수집합니다.",
        category = BeezKitCatalogCategory.Debug,
    ),
    plannedEntry(
        id = "inspector-event",
        modulePath = ":toolkit:inspector:event",
        title = "Inspector Event",
        description = "구조화된 앱 이벤트를 제한된 기록으로 수집합니다.",
        category = BeezKitCatalogCategory.Debug,
    ),
    plannedEntry(
        id = "inspector-webview",
        modulePath = ":toolkit:inspector:webview",
        title = "Inspector WebView",
        description = "선택한 WebView의 콘솔과 탐색 진단 정보를 수집합니다.",
        category = BeezKitCatalogCategory.Debug,
    ),
    plannedEntry(
        id = "toast",
        modulePath = ":components:toast",
        title = "Toast",
        description = "스타일과 표시 정책을 선택할 수 있는 인앱 메시지를 제공합니다.",
        category = BeezKitCatalogCategory.Component,
    ),
    plannedEntry(
        id = "snackbar",
        modulePath = ":components:snackbar",
        title = "Snackbar",
        description = "액션과 결과 처리를 지원하는 브랜드형 Snackbar를 제공합니다.",
        category = BeezKitCatalogCategory.Component,
    ),
    plannedEntry(
        id = "tooltip",
        modulePath = ":components:tooltip",
        title = "Tooltip",
        description = "화면 경계를 고려하는 anchor 기반 Tooltip을 제공합니다.",
        category = BeezKitCatalogCategory.Component,
    ),
    plannedEntry(
        id = "skeleton",
        modulePath = ":components:skeleton",
        title = "Skeleton",
        description = "Composable의 형태를 유지하는 로딩 상태와 화면용 primitive를 제공합니다.",
        category = BeezKitCatalogCategory.Component,
    ),
)

private fun plannedEntry(
    id: String,
    modulePath: String,
    title: String,
    description: String,
    category: BeezKitCatalogCategory,
): BeezKitCatalogEntry = BeezKitCatalogEntry(
    id = id,
    modulePath = modulePath,
    title = title,
    description = description,
    category = category,
    status = BeezKitModuleStatus.Planned,
    content = {
        Text("아직 공개 API가 구현되지 않았습니다.")
    },
)

