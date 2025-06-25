package com.desarrollodroide.adventurelog.feature.collections.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionItem
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsUiState
import com.desarrollodroide.adventurelog.feature.ui.components.ErrorState
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog
import com.desarrollodroide.adventurelog.feature.ui.components.NoSearchResultsState
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
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchSubmit = viewModel::onSearchSubmit,
        modifier = modifier
    )
}

@Composable
private fun CollectionsContent(
    uiState: CollectionsUiState,
    onCollectionClick: (String, String) -> Unit,
    onAddCollectionClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchSubmit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SimpleSearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSearchSubmit = onSearchSubmit,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        },
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
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when {
                uiState.isLoading -> {
                    LoadingDialog(
                        isLoading = true,
                        showMessage = false
                    )
                }
                uiState.errorMessage != null -> {
                    CollectionErrorState(message = uiState.errorMessage)
                }
                uiState.filteredCollections.isEmpty() && uiState.searchQuery.isNotEmpty() -> {
                    CollectionNoSearchResultsState(searchQuery = uiState.searchQuery)
                }
                uiState.collections.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    CollectionList(
                        collections = uiState.filteredCollections,
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
            .fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 80.dp // Extra padding for last item visibility while allowing overscroll
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = collections,
            key = { it.id }
        ) { collection ->
            CollectionItem(
                collection = collection,
                onClick = { onCollectionClick(collection.id, collection.name) }
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No collections yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Create your first collection to organize your adventures!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CollectionNoSearchResultsState(searchQuery: String) {
    NoSearchResultsState(searchQuery = searchQuery)
}

@Composable
private fun CollectionErrorState(message: String) {
    ErrorState(
        message = message,
        onRetry = null // Collections no tiene retry functionality
    )
}

// Previews
@Preview
@Composable
private fun CollectionsScreenLightPreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                CollectionsContent(
                    uiState = CollectionsUiState(
                        collections = PreviewData.collections,
                        filteredCollections = PreviewData.collections
                    ),
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
                    uiState = CollectionsUiState(
                        collections = PreviewData.collections,
                        filteredCollections = PreviewData.collections
                    ),
                    onCollectionClick = { _, _ -> },
                    onAddCollectionClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun CollectionsScreenEmptyPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            CollectionsContent(
                uiState = CollectionsUiState(),
                onCollectionClick = { _, _ -> },
                onAddCollectionClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun CollectionsScreenLoadingPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            CollectionsContent(
                uiState = CollectionsUiState(isLoading = true),
                onCollectionClick = { _, _ -> },
                onAddCollectionClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun CollectionsScreenSearchPreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                CollectionsContent(
                    uiState = CollectionsUiState(
                        collections = PreviewData.collections,
                        filteredCollections = PreviewData.collections.take(1),
                        searchQuery = "Spain"
                    ),
                    onCollectionClick = { _, _ -> },
                    onAddCollectionClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun CollectionsScreenNoResultsPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            CollectionsContent(
                uiState = CollectionsUiState(
                    collections = PreviewData.collections,
                    filteredCollections = emptyList(),
                    searchQuery = "Antarctica"
                ),
                onCollectionClick = { _, _ -> },
                onAddCollectionClick = {}
            )
        }
    }
}
