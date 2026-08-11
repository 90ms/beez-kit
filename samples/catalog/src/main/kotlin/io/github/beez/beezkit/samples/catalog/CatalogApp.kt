package io.github.beez.beezkit.samples.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun BeezKitCatalogApp() {
    var selectedEntryId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedEntry = beezKitCatalogEntries.firstOrNull { it.id == selectedEntryId }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (selectedEntry == null) {
                CatalogListScreen(onEntrySelected = { selectedEntryId = it.id })
            } else {
                CatalogDetailScreen(
                    entry = selectedEntry,
                    onBack = { selectedEntryId = null },
                )
            }
        }
    }
}

@Composable
private fun CatalogListScreen(
    onEntrySelected: (BeezKitCatalogEntry) -> Unit,
) {
    Scaffold { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "BeezKit Catalog",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "호스트 앱에서 사용할 공개 API와 동작을 모듈별로 확인합니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            BeezKitCatalogCategory.entries.forEach { category ->
                item(key = "header-${category.name}") {
                    Text(
                        text = category.label,
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(
                    items = beezKitCatalogEntries.filter { it.category == category },
                    key = BeezKitCatalogEntry::id,
                ) { entry ->
                    CatalogEntryCard(entry = entry, onClick = { onEntrySelected(entry) })
                }
            }
        }
    }
}

@Composable
private fun CatalogEntryCard(
    entry: BeezKitCatalogEntry,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                StatusLabel(entry.status)
            }
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = entry.modulePath,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun CatalogDetailScreen(
    entry: BeezKitCatalogEntry,
    onBack: () -> Unit,
) {
    Scaffold { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Button(onClick = onBack) {
                    Text("목록으로")
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        StatusLabel(entry.status)
                    }
                    Text(entry.description, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = entry.modulePath,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("샘플", style = MaterialTheme.typography.titleMedium)
                        entry.content()
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusLabel(status: BeezKitModuleStatus) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

