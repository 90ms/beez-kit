package io.github.beez.beezkit.samples.catalog

import androidx.compose.runtime.Composable

internal enum class BeezKitCatalogCategory(
    val label: String,
) {
    Toolkit("툴킷"),
    Debug("디버그"),
    Component("컴포넌트"),
}

internal enum class BeezKitModuleStatus(
    val label: String,
) {
    Planned("예정"),
    Experimental("실험적"),
    Stable("안정"),
}

internal data class BeezKitCatalogEntry(
    val id: String,
    val modulePath: String,
    val title: String,
    val description: String,
    val category: BeezKitCatalogCategory,
    val status: BeezKitModuleStatus,
    val content: @Composable () -> Unit,
)
