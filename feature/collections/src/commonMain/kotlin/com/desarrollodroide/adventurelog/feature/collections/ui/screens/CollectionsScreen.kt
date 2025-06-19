package com.desarrollodroide.adventurelog.feature.collections.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsUiState
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog
import com.desarrollodroide.adventurelog.feature.ui.preview.PreviewImageDependencies
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CollectionsScreen(
    onCollectionClick: (String, String) -> Unit = { _, _ -> },
    onAddCollectionClick: () -> Unit = { },
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    CollectionsContent(
        uiState = uiState,
        onCollectionClick = onCollectionClick,
        onAddCollectionClick = onAddCollectionClick,
        modifier = modifier
    )
}

@Composable
private fun CollectionsContent(
    uiState: CollectionsUiState,
    onCollectionClick: (String, String) -> Unit,
    onAddCollectionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCollectionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add collection"
                )
            }
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingDialog(
                        isLoading = true,
                        showMessage = false
                    )
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
                        onCollectionClick = onCollectionClick
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionList(
    collections: List<Collection>,
    onCollectionClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(collections) { collection ->
            CollectionItem(
                collection = collection,
                onClick = { onCollectionClick(collection.id, collection.name) }
            )
        }
        
        // Add extra space at the bottom to ensure FAB doesn't cover the last item
        item {
            Spacer(modifier = Modifier.height(72.dp))
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
                CollectionsContent(
                    uiState = CollectionsUiState(collections = PreviewData.collections),
                    onCollectionClick = { _, _ -> },
                    onAddCollectionClick = {}
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
                CollectionsContent(
                    uiState = CollectionsUiState(collections = PreviewData.collections),
                    onCollectionClick = { _, _ -> },
                    onAddCollectionClick = {}
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
                CollectionsContent(
                    uiState = CollectionsUiState(collections = PreviewData.spainRegionsCollections),
                    onCollectionClick = { _, _ -> },
                    onAddCollectionClick = {}
                )
            }
        }
    }
}
