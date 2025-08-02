package com.desarrollodroide.adventurelog.feature.collections.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionItem
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.ErrorState
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingCard
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CollectionsScreen(
    onCollectionClick: (String, String) -> Unit = { _, _ -> },
    onAddCollectionClick: () -> Unit = { },
    onEditCollection: (Collection) -> Unit = { },
    onPagingItemsReady: (LazyPagingItems<Collection>) -> Unit = { },
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = koinViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val pagingItems = viewModel.collectionsPagingData.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()

    var collectionToDelete by remember { mutableStateOf<Collection?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Notify when paging items are ready
    LaunchedEffect(pagingItems) {
        onPagingItemsReady(pagingItems)
    }

    CollectionsContent(
        pagingItems = pagingItems,
        searchQuery = searchQuery,
        isRefreshing = isRefreshing,
        snackbarHostState = snackbarHostState,
        onCollectionClick = onCollectionClick,
        onAddCollectionClick = onAddCollectionClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onEditCollection = onEditCollection,
        onDeleteCollection = { collection -> collectionToDelete = collection },
        onRefresh = {
            viewModel.refresh()
            pagingItems.refresh()
        },
        modifier = modifier
    )

    LaunchedEffect(pagingItems.loadState.refresh) {
        if (pagingItems.loadState.refresh is LoadStateNotLoading) {
            viewModel.onRefreshComplete()
        }
    }

    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is CollectionsViewModel.DeleteState.Success -> {
                pagingItems.refresh()
                snackbarHostState.showSnackbar("Collection deleted successfully")
                viewModel.clearDeleteState()
            }

            is CollectionsViewModel.DeleteState.Error -> {
                snackbarHostState.showSnackbar("Error: ${state.message}")
                viewModel.clearDeleteState()
            }

            else -> {}
        }
    }

    // Delete confirmation dialog
    collectionToDelete?.let { collection ->
        AlertDialog(
            onDismissRequest = { collectionToDelete = null },
            title = { Text("Delete Collection") },
            text = { Text("Are you sure you want to delete \"${collection.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCollection(collection.id)
                        collectionToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { collectionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionsContent(
    pagingItems: LazyPagingItems<Collection>,
    searchQuery: String,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
    onCollectionClick: (String, String) -> Unit,
    onAddCollectionClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEditCollection: (Collection) -> Unit,
    onDeleteCollection: (Collection) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier,
        topBar = {
            SimpleSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSearchSubmit = { },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when {
                // Show existing content during pull to refresh
                isRefreshing && pagingItems.itemCount > 0 -> {
                    CollectionsPagingList(
                        pagingItems = pagingItems,
                        onCollectionClick = onCollectionClick,
                        onEditCollection = onEditCollection,
                        onDeleteCollection = onDeleteCollection
                    )
                }

                pagingItems.loadState.refresh is LoadStateLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingCard(
                            message = "Loading collections...",
                            showOverlay = false
                        )
                    }
                }

                pagingItems.loadState.refresh is LoadStateError -> {
                    val error = pagingItems.loadState.refresh as LoadStateError
                    ErrorState(
                        message = error.error.message ?: "Unknown error",
                        onRetry = { pagingItems.retry() }
                    )
                }

                pagingItems.loadState.refresh is LoadStateNotLoading -> {
                    when {
                        pagingItems.itemCount == 0 && searchQuery.isEmpty() -> {
                            EmptyState()
                        }

                        pagingItems.itemCount == 0 && searchQuery.isNotEmpty() -> {
                            NoSearchResultsState(searchQuery = searchQuery)
                        }

                        else -> {
                            CollectionsPagingList(
                                pagingItems = pagingItems,
                                onCollectionClick = onCollectionClick,
                                onEditCollection = onEditCollection,
                                onDeleteCollection = onDeleteCollection
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionsPagingList(
    pagingItems: LazyPagingItems<Collection>,
    onCollectionClick: (String, String) -> Unit,
    onEditCollection: (Collection) -> Unit,
    onDeleteCollection: (Collection) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id }
        ) { index ->
            val collection = pagingItems[index]
            if (collection != null) {
                CollectionItem(
                    collection = collection,
                    onClick = { onCollectionClick(collection.id, collection.name) },
                    onEditCollection = { onEditCollection(collection) },
                    onDeleteCollection = { onDeleteCollection(collection) }
                )
            }
        }

        // Load state for append (loading more items)
        when (pagingItems.loadState.append) {
            is LoadStateLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            is LoadStateError -> {
                val error = pagingItems.loadState.append as LoadStateError
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading more: ${error.error.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            is LoadStateNotLoading -> {
                // Do nothing
            }
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
private fun NoSearchResultsState(searchQuery: String) {
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
            Text(
                text = "No results for \"$searchQuery\"",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Try searching with different keywords",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
