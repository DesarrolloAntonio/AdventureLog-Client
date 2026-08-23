package com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.locations.ui.components.LocationsFilterBottomSheet
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem
import com.desarrollodroide.adventurelog.feature.ui.components.ErrorState
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingCard
import com.desarrollodroide.adventurelog.feature.ui.components.ManageCollectionsDialog
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LocationListScreen(
    onAdventureClick: (Location) -> Unit = { },
    onAddAdventureClick: () -> Unit = { },
    onEditAdventure: (Location) -> Unit = { },
    modifier: Modifier = Modifier,
    viewModel: LocationsViewModel = koinViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val actualSearchQuery by viewModel.actualSearchQuery.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val showFilters by viewModel.showFilters.collectAsStateWithLifecycle()
    val categoriesState by viewModel.categoriesState.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val collectionsLoading by viewModel.collectionsLoading.collectAsStateWithLifecycle()
    val pagingItems = viewModel.adventuresPagingData.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val updateCollectionsState by viewModel.updateCollectionsState.collectAsStateWithLifecycle()
    val categoryOperationState by viewModel.categoryOperationState.collectAsStateWithLifecycle()

    var locationToManageCollections by remember { mutableStateOf<Location?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val libraryCounts by viewModel.libraryCounts.collectAsStateWithLifecycle()

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

        if (showFilters) {
            LocationsFilterBottomSheet(
                filters = filters,
                categoriesState = categoriesState,
                onFiltersChanged = viewModel::onFiltersChanged,
                onDismiss = viewModel::hideFilters,
                onManageCategoriesClick = {
                    viewModel.retryLoadCategories()
                },
                onRetryLoadCategories = viewModel::retryLoadCategories,
                onAddCategory = viewModel::createCategory,
                onUpdateCategory = viewModel::updateCategory,
                onDeleteCategory = viewModel::deleteCategory
            )
        }

    AdventureListContent(
        pagingItems = pagingItems,
        searchQuery = searchQuery,
        actualSearchQuery = actualSearchQuery,
        hasActiveFilters = viewModel.hasActiveFilters(),
        collections = collections,
        isRefreshing = isRefreshing,
        snackbarHostState = snackbarHostState,
        onAdventureClick = { adventure ->
            viewModel.selectLocation(adventure)
            onAdventureClick(adventure)
        },
        onAddAdventureClick = onAddAdventureClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchSubmit = viewModel::executeSearch,
        onShowFilters = viewModel::showFilters,
        onEditAdventure = onEditAdventure,
        onDuplicateAdventure = viewModel::duplicateLocation,
        onShareAdventure = viewModel::shareLocation,
        onDeleteAdventure = { adventure -> 
            viewModel.deleteAdventure(adventure.id)
        },
        libraryCounts = libraryCounts,
        onManageCollections = { adventure -> locationToManageCollections = adventure },
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
            is LocationsViewModel.DeleteState.Success -> {
                pagingItems.refresh()
                snackbarHostState.showSnackbar("Location deleted successfully")
                viewModel.clearDeleteState()
            }

            is LocationsViewModel.DeleteState.Error -> {
                snackbarHostState.showSnackbar("Error: ${state.message}")
                viewModel.clearDeleteState()
            }

            else -> {}
        }
    }
    
    LaunchedEffect(updateCollectionsState) {
        when (val state = updateCollectionsState) {
            is LocationsViewModel.UpdateCollectionsState.Success -> {
                pagingItems.refresh()
                snackbarHostState.showSnackbar("Collections updated successfully")
                viewModel.clearUpdateCollectionsState()
            }

            is LocationsViewModel.UpdateCollectionsState.Error -> {
                snackbarHostState.showSnackbar("Error updating collections: ${state.message}")
                viewModel.clearUpdateCollectionsState()
            }

            else -> {}
        }
    }
    
    LaunchedEffect(categoryOperationState) {
        when (val state = categoryOperationState) {
            is LocationsViewModel.CategoryOperationState.Success -> {
                snackbarHostState.showSnackbar("Category operation successful")
                viewModel.clearCategoryOperationState()
            }

            is LocationsViewModel.CategoryOperationState.Error -> {
                snackbarHostState.showSnackbar("Error: ${state.message}")
                viewModel.clearCategoryOperationState()
            }

            else -> {}
        }
    }

    // Manage Collections dialog
    locationToManageCollections?.let { adventure ->
        ManageCollectionsDialog(
            location = adventure,
            allCollections = collections,
            isLoadingCollections = collectionsLoading,
            onUpdateCollections = { adventureId, collectionIds ->
                viewModel.updateAdventureCollections(adventureId, collectionIds)
                locationToManageCollections = null
            },
            onRefreshCollections = {
                viewModel.refreshCollections()
            },
            onDismiss = { locationToManageCollections = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdventureListContent(
    pagingItems: LazyPagingItems<Location>,
    searchQuery: String,
    actualSearchQuery: String,
    hasActiveFilters: Boolean,
    collections: List<UltraSlimCollection>,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
    onAdventureClick: (Location) -> Unit,
    onAddAdventureClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onShowFilters: () -> Unit,
    onEditAdventure: (Location) -> Unit,
    onDuplicateAdventure: (Location) -> Unit,
    onShareAdventure: (Location) -> Unit,
    onDeleteAdventure: (Location) -> Unit,
    onManageCollections: (Location) -> Unit,
    onRefresh: () -> Unit,
    libraryCounts: UserStats? = null,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier,
        topBar = {
          Column {
            // The whole library, not the page on screen - see LocationsViewModel.libraryCounts.
            libraryCounts?.let { counts ->
                Text(
                    text = buildString {
                        append(counts.locationCount)
                        append(if (counts.locationCount == 1) " location" else " locations")
                        if (counts.visitedLocationCount > 0) {
                            append(" · ")
                            append(counts.visitedLocationCount)
                            append(" visited")
                        }
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearchSubmit = onSearchSubmit,
                    activeSearchQuery = actualSearchQuery,
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
                    contentDescription = "Add location"
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
                    AdventuresPagingList(
                        pagingItems = pagingItems,
                        collections = collections,
                        onAdventureClick = onAdventureClick,
                        onEditAdventure = onEditAdventure,
                        onDuplicateAdventure = onDuplicateAdventure,
                        onShareAdventure = onShareAdventure,
                        onDeleteAdventure = onDeleteAdventure,
                        onManageCollections = onManageCollections
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
                        // The search that is actually in effect, not the text still in the box -
                        // submitting clears the box, which used to make an empty result read as
                        // "no locations yet, create your first one".
                        pagingItems.itemCount == 0 && actualSearchQuery.isEmpty() && !hasActiveFilters -> {
                            EmptyState()
                        }

                        pagingItems.itemCount == 0 -> {
                            NoSearchResultsState(
                                searchQuery = actualSearchQuery,
                                hasFilters = hasActiveFilters
                            )
                        }

                        else -> {
                            AdventuresPagingList(
                                pagingItems = pagingItems,
                                collections = collections,
                                onAdventureClick = onAdventureClick,
                                onEditAdventure = onEditAdventure,
                                onDuplicateAdventure = onDuplicateAdventure,
                                onShareAdventure = onShareAdventure,
                                onDeleteAdventure = onDeleteAdventure,
                                onManageCollections = onManageCollections
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
    pagingItems: LazyPagingItems<Location>,
    collections: List<UltraSlimCollection>,
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    onDuplicateAdventure: (Location) -> Unit,
    onShareAdventure: (Location) -> Unit,
    onDeleteAdventure: (Location) -> Unit,
    onManageCollections: (Location) -> Unit
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
                    location = adventure,
                    onClick = { onAdventureClick(adventure) },
                    onEdit = { onEditAdventure(adventure) },
                    onDuplicate = { onDuplicateAdventure(adventure) },
                    onShare = { onShareAdventure(adventure) },
                    onDelete = { onDeleteAdventure(adventure) },
                    onManageCollections = { onManageCollections(adventure) }
                )
            }
        }

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
                // Nothing to do
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
                text = "No locations yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Start exploring and add your first location!",
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
