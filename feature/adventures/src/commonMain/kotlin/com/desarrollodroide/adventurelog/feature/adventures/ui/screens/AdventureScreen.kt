package com.desarrollodroide.adventurelog.feature.adventures.ui.screens

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
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
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
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AdventuresUiState
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AdventuresViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog
import com.desarrollodroide.adventurelog.feature.ui.preview.PreviewImageDependencies
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AdventureListScreen(
    onAdventureClick: (Adventure) -> Unit = { },
    onAddAdventureClick: () -> Unit = { },
    modifier: Modifier = Modifier,
    viewModel: AdventuresViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    AdventureListContent(
        uiState = uiState,
        onAdventureClick = onAdventureClick,
        onAddAdventureClick = onAddAdventureClick,
        modifier = modifier
    )
}

@Composable
private fun AdventureListContent(
    uiState: AdventuresUiState,
    onAdventureClick: (Adventure) -> Unit,
    onAddAdventureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
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
                    if (uiState.adventures.isEmpty()) {
                        EmptyState()
                    } else {
                        AdventuresList(
                            adventures = uiState.adventures,
                            onAdventureClick = onAdventureClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdventuresList(
    adventures: List<Adventure>,
    onAdventureClick: (Adventure) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(adventures) { adventure ->
            AdventureItem(
                adventure = adventure,
                onClick = { onAdventureClick(adventure) }
            )
        }
        
        // Add extra space at the bottom to ensure FAB doesn't cover the last item
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

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
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
                imageVector = Icons.Default.SentimentDissatisfied,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("Try Again")
            }
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
                    uiState = AdventuresUiState.Success(PreviewData.adventures),
                    onAdventureClick = {},
                    onAddAdventureClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun AdventureListScreenDarkThemePreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            PreviewImageDependencies {
                AdventureListContent(
                    uiState = AdventuresUiState.Success(PreviewData.adventures),
                    onAdventureClick = {},
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
                onAdventureClick = {},
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
                onAdventureClick = {},
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
                uiState = AdventuresUiState.Error("Failed to load adventures. Please check your connection and try again."),
                onAdventureClick = {},
                onAddAdventureClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun AdventureListScreenSingleItemPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            PreviewImageDependencies {
                AdventureListContent(
                    uiState = AdventuresUiState.Success(listOf(PreviewData.adventures.first())),
                    onAdventureClick = {},
                    onAddAdventureClick = {}
                )
            }
        }
    }
}
