package com.desarrollodroide.adventurelog.feature.collections.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionItem
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import com.desarrollodroide.adventurelog.feature.ui.preview.PreviewImageDependencies
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CollectionsScreen(
    onCollectionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${uiState.errorMessage}")
            }
        }
        else -> {
            CollectionList(
                collections = uiState.collections,
                onCollectionClick = onCollectionClick,
                modifier = modifier
            )
        }
    }
}

@Composable
fun CollectionList(
    collections: List<Collection>,
    onCollectionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        collections.forEach { collection ->
            CollectionItem(
                collection = collection,
                onClick = { onCollectionClick(collection.id) }
            )
        }
    }
}

// Previews
@Preview
@Composable
private fun CollectionsScreenLightPreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                CollectionList(
                    collections = PreviewData.collections,
                    onCollectionClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun CollectionsScreenDarkPreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                CollectionList(
                    collections = PreviewData.collections,
                    onCollectionClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun CollectionsScreenSpainRegionsPreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                CollectionList(
                    collections = PreviewData.spainRegionsCollections,
                    onCollectionClick = {}
                )
            }
        }
    }
}
