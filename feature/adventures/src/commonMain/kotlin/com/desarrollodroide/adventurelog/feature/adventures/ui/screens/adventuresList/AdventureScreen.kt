package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.adventuresList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.feature.adventures.ui.components.AdventuresFilterBottomSheet
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AdventuresViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem
import com.desarrollodroide.adventurelog.feature.ui.components.ErrorState
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingCard
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AdventureListScreen(
    onAdventureClick: (Adventure, List<Collection>) -> Unit = { _, _ -> },
    onAddAdventureClick: () -> Unit = { },
    onManageCategoriesClick: () -> Unit = { },
    collections: List<Collection> = emptyList(),
    modifier: Modifier = Modifier,
    viewModel: AdventuresViewModel = koinViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val showFilters by viewModel.showFilters.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val pagingItems = viewModel.adventuresPagingData.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    if (showFilters) {
        AdventuresFilterBottomSheet(
            filters = filters,
            categories = categories,
            onFiltersChanged = viewModel::onFiltersChanged,
            onDismiss = viewModel::hideFilters,
            onManageCategoriesClick = onManageCategoriesClick
        )
    }

    AdventureListContent(
        pagingItems = pagingItems,
        searchQuery = searchQuery,
        hasActiveFilters = viewModel.hasActiveFilters(),
        collections = collections,
        isRefreshing = isRefreshing,
        onAdventureClick = onAdventureClick,
        onAddAdventureClick = onAddAdventureClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onShowFilters = viewModel::showFilters,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdventureListContent(
    pagingItems: LazyPagingItems<Adventure>,
    searchQuery: String,
    hasActiveFilters: Boolean,
    collections: List<Collection>,
    isRefreshing: Boolean,
    onAdventureClick: (Adventure, List<Collection>) -> Unit,
    onAddAdventureClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onShowFilters: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearchSubmit = { },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onShowFilters,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (hasActiveFilters) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        if (hasActiveFilters) {
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd),
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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
                    AdventuresPagingList(
                        pagingItems = pagingItems,
                        collections = collections,
                        onAdventureClick = { adventure ->
                            val adventureCollections = adventure.collections.mapNotNull { id ->
                                collections.find { it.id == id }
                            }
                            onAdventureClick(adventure, adventureCollections)
                        }
                    )
                }
                
                pagingItems.loadState.refresh is LoadStateLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingCard(
                            message = "Loading adventures...",
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
                        pagingItems.itemCount == 0 && searchQuery.isEmpty() && !hasActiveFilters -> {
                            EmptyState()
                        }

                        pagingItems.itemCount == 0 && (searchQuery.isNotEmpty() || hasActiveFilters) -> {
                            NoSearchResultsState(
                                searchQuery = searchQuery,
                                hasFilters = hasActiveFilters
                            )
                        }

                        else -> {
                            AdventuresPagingList(
                                pagingItems = pagingItems,
                                collections = collections,
                                onAdventureClick = { adventure ->
                                    val adventureCollections =
                                        adventure.collections.mapNotNull { id ->
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
private fun AdventuresPagingList(
    pagingItems: LazyPagingItems<Adventure>,
    collections: List<Collection>,
    onAdventureClick: (Adventure) -> Unit
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
            val adventure = pagingItems[index]
            if (adventure != null) {
                AdventureItem(
                    adventure = adventure,
                    collections = collections,
                    onClick = { onAdventureClick(adventure) }
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
private fun NoSearchResultsState(
    searchQuery: String,
    hasFilters: Boolean
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
            Text(
                text = when {
                    searchQuery.isNotEmpty() && hasFilters -> "No results for \"$searchQuery\" with current filters"
                    searchQuery.isNotEmpty() -> "No results for \"$searchQuery\""
                    else -> "No results with current filters"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = when {
                    hasFilters -> "Try adjusting your filters or search terms"
                    else -> "Try searching with different keywords"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
