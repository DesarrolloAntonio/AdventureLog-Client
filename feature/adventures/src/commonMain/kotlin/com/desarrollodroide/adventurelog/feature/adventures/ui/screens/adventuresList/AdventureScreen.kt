package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.adventuresList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AdventuresUiState
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AdventuresViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem
import com.desarrollodroide.adventurelog.feature.ui.components.ErrorState
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog
import com.desarrollodroide.adventurelog.feature.ui.components.NoSearchResultsState
import com.desarrollodroide.adventurelog.feature.ui.preview.PreviewImageDependencies
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AdventureListScreen(
    onAdventureClick: (Adventure, List<Collection>) -> Unit = { _, _ -> },
    onAddAdventureClick: () -> Unit = { },
    collections: List<Collection> = emptyList(),
    modifier: Modifier = Modifier,
    viewModel: AdventuresViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    AdventureListContent(
        uiState = uiState,
        collections = collections,
        onAdventureClick = onAdventureClick,
        onAddAdventureClick = onAddAdventureClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchSubmit = viewModel::onSearchSubmit,
        modifier = modifier
    )
}

@Composable
private fun AdventureListContent(
    uiState: AdventuresUiState,
    collections: List<Collection>,
    onAdventureClick: (Adventure, List<Collection>) -> Unit,
    onAddAdventureClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchSubmit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            when (uiState) {
                is AdventuresUiState.Success -> {
                    SimpleSearchBar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        onSearchSubmit = onSearchSubmit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    SimpleSearchBar(
                        searchQuery = "",
                        onSearchQueryChange = {},
                        onSearchSubmit = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAdventureClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add adventure"
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is AdventuresUiState.Loading -> {
                    LoadingDialog(
                        isLoading = true,
                        showMessage = false
                    )
                }
                is AdventuresUiState.Error -> {
                    ErrorState(
                        message = uiState.message,
                        onRetry = { /* TODO: Add retry functionality */ }
                    )
                }
                is AdventuresUiState.Success -> {
                    when {
                        uiState.filteredAdventures.isEmpty() && uiState.searchQuery.isNotEmpty() -> {
                            NoSearchResultsState(searchQuery = uiState.searchQuery)
                        }
                        uiState.adventures.isEmpty() -> {
                            EmptyState()
                        }
                        else -> {
                            AdventuresList(
                                adventures = uiState.filteredAdventures,
                                collections = collections,
                                onAdventureClick = { adventure ->
                                    val adventureCollections = adventure.collections.mapNotNull { id ->
                                        collections.find { it.id == id }
                                    }
                                    onAdventureClick(adventure, adventureCollections)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdventuresList(
    adventures: List<Adventure>,
    collections: List<Collection>,
    onAdventureClick: (Adventure) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = adventures,
            key = { it.id }
        ) { adventure ->
            AdventureItem(
                adventure = adventure,
                collections = collections,
                onClick = { onAdventureClick(adventure) }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(72.dp))
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
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No adventures yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Start exploring and create your first adventure!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Previews
@Preview
@Composable
private fun AdventureListScreenSuccessPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            PreviewImageDependencies {
                AdventureListContent(
                    uiState = AdventuresUiState.Success(
                        adventures = PreviewData.adventures,
                        filteredAdventures = PreviewData.adventures
                    ),
                    collections = PreviewData.collections,
                    onAdventureClick = { _, _ -> },
                    onAddAdventureClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun AdventureListScreenEmptyPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AdventureListContent(
                uiState = AdventuresUiState.Success(emptyList()),
                collections = emptyList(),
                onAdventureClick = { _, _ -> },
                onAddAdventureClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun AdventureListScreenLoadingPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AdventureListContent(
                uiState = AdventuresUiState.Loading,
                collections = emptyList(),
                onAdventureClick = { _, _ -> },
                onAddAdventureClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun AdventureListScreenErrorPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AdventureListContent(
                uiState = AdventuresUiState.Error("Failed to load adventures"),
                collections = emptyList(),
                onAdventureClick = { _, _ -> },
                onAddAdventureClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun AdventureListScreenSearchPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            PreviewImageDependencies {
                AdventureListContent(
                    uiState = AdventuresUiState.Success(
                        adventures = PreviewData.adventures,
                        filteredAdventures = PreviewData.adventures.take(1),
                        searchQuery = "Mountain"
                    ),
                    collections = PreviewData.collections,
                    onAdventureClick = { _, _ -> },
                    onAddAdventureClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun AdventureListScreenNoResultsPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AdventureListContent(
                uiState = AdventuresUiState.Success(
                    adventures = PreviewData.adventures,
                    filteredAdventures = emptyList(),
                    searchQuery = "Antarctica"
                ),
                collections = PreviewData.collections,
                onAdventureClick = { _, _ -> },
                onAddAdventureClick = {}
            )
        }
    }
}
