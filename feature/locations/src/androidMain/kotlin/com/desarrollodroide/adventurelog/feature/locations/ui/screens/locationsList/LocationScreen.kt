@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList

import androidx.compose.material3.LoadingIndicator
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel.CategoriesState
import com.desarrollodroide.adventurelog.feature.locations.ui.components.categories.ManageCategoriesDialog
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import com.desarrollodroide.adventurelog.feature.ui.components.rememberShellAction
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
import com.desarrollodroide.adventurelog.feature.ui.components.PlaceRow
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import androidx.compose.foundation.lazy.LazyColumn
import com.desarrollodroide.adventurelog.feature.locations.model.VisitedFilter
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.feature.locations.model.LocationSortField
import com.desarrollodroide.adventurelog.feature.ui.components.ErrorState
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingCard
import com.desarrollodroide.adventurelog.feature.ui.components.ManageCollectionsDialog
import com.desarrollodroide.adventurelog.feature.ui.components.SearchBarAction
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn

@Composable
fun LocationListScreen(
    onAdventureClick: (Location) -> Unit = { },
    onAddAdventureClick: () -> Unit = { },
    onEditAdventure: (Location) -> Unit = { },
    onAddPhoto: (Location) -> Unit = onEditAdventure,
    /**
     * Called once with the first place the list loads. Two-pane callers use it to fill the detail
     * side, which otherwise sits empty until something is tapped.
     */
    onFirstLoaded: (Location) -> Unit = { },
    /**
     * Called with the id of a place that has just been deleted. A two-pane caller showing that
     * place has to drop it: the pane kept a place that no longer existed (measured).
     */
    onPlaceDeleted: (String) -> Unit = { },
    modifier: Modifier = Modifier,
    viewModel: LocationsViewModel = koinViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val actualSearchQuery by viewModel.actualSearchQuery.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val countryGroups by viewModel.countryGroups.collectAsStateWithLifecycle()
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
    var showSort by remember { mutableStateOf(false) }
    var showCategories by remember { mutableStateOf(false) }
    var showManageCategories by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val libraryCounts by viewModel.libraryCounts.collectAsStateWithLifecycle()

    var announcedFirst by remember { mutableStateOf(false) }
    LaunchedEffect(pagingItems.itemCount, announcedFirst) {
        if (!announcedFirst && pagingItems.itemCount > 0) {
            pagingItems.peek(0)?.let {
                announcedFirst = true
                onFirstLoaded(it)
            }
        }
    }

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

        if (showCategories) {
            PlacesCategorySheet(
                categoriesState = categoriesState,
                selected = filters.categoryNames,
                onChange = { viewModel.onFiltersChanged(filters.copy(categoryNames = it)) },
                onManage = { showManageCategories = true },
                onRetry = viewModel::retryLoadCategories,
                onDismiss = { showCategories = false },
            )
        }
        if (showManageCategories) {
            ManageCategoriesDialog(
                categoriesState = categoriesState,
                onDismiss = { showManageCategories = false },
                onAddCategory = viewModel::createCategory,
                onEditCategory = viewModel::updateCategory,
                onDeleteCategory = viewModel::deleteCategory,
                onRetryLoadCategories = viewModel::retryLoadCategories,
            )
        }
        if (showSort) {
            PlacesSortSheet(
                sortField = filters.sortField,
                sortDirection = filters.sortDirection,
                onChange = { field, direction ->
                    viewModel.onFiltersChanged(filters.copy(sortField = field, sortDirection = direction))
                },
                onDismiss = { showSort = false },
            )
        }

    AdventureListContent(
        pagingItems = pagingItems,
        searchQuery = searchQuery,
        actualSearchQuery = actualSearchQuery,
        activeFilterCount = viewModel.activeFilterCount(),
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
        onAddPhoto = onAddPhoto,
        onDuplicateAdventure = viewModel::duplicateLocation,
        onShareAdventure = viewModel::shareLocation,
        onDeleteAdventure = { adventure -> 
            viewModel.deleteAdventure(adventure.id)
        },
        libraryCounts = libraryCounts,
        visitedFilter = filters.visitedFilter,
        onVisitedFilterChange = { viewModel.onFiltersChanged(filters.copy(visitedFilter = it)) },
        // The chip reads the names people gave them ("Hiking"), not the server's slugs ("hiking").
        categoryNames = filters.categoryNames.map { name ->
            (categoriesState as? CategoriesState.Success)?.categories?.firstOrNull { it.name == name }?.displayName ?: name
        },
        sortField = filters.sortField,
        sortDirection = filters.sortDirection,
        onShowSort = { showSort = true },
        countryGroups = countryGroups,
        onShowCategories = { showCategories = true },
        onManageCollections = { adventure -> locationToManageCollections = adventure },
        onRefresh = {
            viewModel.refresh()
            pagingItems.refresh()
        },
        modifier = modifier
    )

    LaunchedEffect(pagingItems.loadState.refresh) {
        val refresh = pagingItems.loadState.refresh
        if (refreshHasEnded(refresh)) viewModel.onRefreshComplete()
        if (refresh is LoadStateNotLoading) {
            // Adding or removing a place reloads the list; the header counts come from a
            // different call and would otherwise still be reporting the number from before.
            viewModel.loadLibraryCounts()
        }
        if (refresh is LoadStateError && pagingItems.itemCount > 0) {
            // The places already shown stay; say why they weren't refreshed.
            snackbarHostState.showSnackbar(placesLoadErrorMessage(refresh.error))
        }
    }

    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is LocationsViewModel.DeleteState.Success -> {
                onPlaceDeleted(state.locationId)
                pagingItems.refresh()
                snackbarHostState.showSnackbar("Place deleted")
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
    activeFilterCount: Int,
    collections: List<UltraSlimCollection>,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
    onAdventureClick: (Location) -> Unit,
    onAddAdventureClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onShowFilters: () -> Unit,
    onEditAdventure: (Location) -> Unit,
    onAddPhoto: (Location) -> Unit = onEditAdventure,
    onDuplicateAdventure: (Location) -> Unit,
    onShareAdventure: (Location) -> Unit,
    onDeleteAdventure: (Location) -> Unit,
    onManageCollections: (Location) -> Unit,
    onRefresh: () -> Unit,
    libraryCounts: UserStats? = null,
    visitedFilter: VisitedFilter = VisitedFilter.ALL,
    onVisitedFilterChange: (VisitedFilter) -> Unit = {},
    categoryNames: List<String> = emptyList(),
    sortField: LocationSortField = LocationSortField.UPDATED_AT,
    sortDirection: SortDirection = SortDirection.DESCENDING,
    onShowSort: () -> Unit = onShowFilters,
    /** Non-null while "Country" is the order: the list is grouped instead of paged. */
    countryGroups: LocationsViewModel.CountryGroupsState? = null,
    onShowCategories: () -> Unit = onShowFilters,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    val handedToShell = rememberShellAction(Icons.Default.Add, "Add a place", onAddAdventureClick)
    val (view, onViewChange) = rememberPlacesView()
    Scaffold(
        modifier = modifier,
        topBar = {
          Column {
            // The whole library, not the page on screen - see LocationsViewModel.libraryCounts.
            libraryCounts?.let { counts ->
                // Places and countries; the visited count is on the Visited button just below.
                Text(
                    text = buildString {
                        append(counts.locationCount)
                        append(if (counts.locationCount == 1) " place" else " places")
                        if (counts.visitedCountryCount > 0) {
                            append(" · ")
                            append(counts.visitedCountryCount)
                            append(if (counts.visitedCountryCount == 1) " country visited" else " countries visited")
                        }
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp)
                )
            }
            val sheetFilterCount = activeFilterCount - if (visitedFilter != VisitedFilter.ALL) 1 else 0
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
                    appliedQuery = actualSearchQuery,
                    placeholder = "Search places",
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                PlacesViewToggle(view, onViewChange)
            }
            VisitedFilterGroup(
                counts = libraryCounts,
                selected = visitedFilter,
                onSelect = onVisitedFilterChange,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )
            // The filters the sheet holds, said as chips, as the canvas draws them: what is on
            // reads at a glance, and each one opens the sheet.
            PlacesFilterChips(
                sheetFilterCount = sheetFilterCount,
                categoryNames = categoryNames,
                sortField = sortField,
                sortDirection = sortDirection,
                onOpen = onShowFilters,
                onOpenSort = onShowSort,
                onOpenCategories = onShowCategories,
                modifier = Modifier.padding(bottom = 8.dp)
            )
          }
        },
        floatingActionButton = {
            // On a phone the shell draws this beside its floating toolbar, as the canvas does.
            if (!handedToShell) FloatingActionButton(
                onClick = onAddAdventureClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add a place"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        // The bottom bar's Scaffold has already inset this content, but plain padding does not
        // consume a window inset - so asking for systemBars here counted the gesture bar a second
        // time and floated the button a finger's width clear of where it belongs.
        contentWindowInsets = WindowInsets(0)
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
                countryGroups != null -> when (countryGroups) {
                    is LocationsViewModel.CountryGroupsState.Loading -> CountryGroupsLoading()
                    is LocationsViewModel.CountryGroupsState.Error -> ErrorState(message = countryGroups.message, onRetry = onRefresh)
                    is LocationsViewModel.CountryGroupsState.Ready -> if (countryGroups.groups.isEmpty()) {
                        NoSearchResultsState(searchQuery = actualSearchQuery, hasFilters = activeFilterCount > 0)
                    } else {
                        CountryGroupedList(
                            groups = countryGroups.groups,
                            onPlaceClick = onAdventureClick,
                            onEdit = onEditAdventure,
                            onAddPhoto = onAddPhoto,
                            onDuplicate = onDuplicateAdventure,
                            onShare = onShareAdventure,
                            onDelete = onDeleteAdventure,
                            onManageCollections = onManageCollections,
                        )
                    }
                }

                // Show existing content during pull to refresh
                isRefreshing && pagingItems.itemCount > 0 -> {
                    AdventuresPagingList(
                        pagingItems = pagingItems,
                        view = view,
                        collections = collections,
                        onAdventureClick = onAdventureClick,
                        onEditAdventure = onEditAdventure,
                        onAddPhoto = onAddPhoto,
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
                            message = "Loading places...",
                            showOverlay = false
                        )
                    }
                }

                // A failed refresh over places already shown keeps them on screen.
                pagingItems.loadState.refresh is LoadStateError && pagingItems.itemCount > 0 -> {
                    AdventuresPagingList(
                        pagingItems = pagingItems,
                        view = view,
                        collections = collections,
                        onAdventureClick = onAdventureClick,
                        onEditAdventure = onEditAdventure,
                        onAddPhoto = onAddPhoto,
                        onDuplicateAdventure = onDuplicateAdventure,
                        onShareAdventure = onShareAdventure,
                        onDeleteAdventure = onDeleteAdventure,
                        onManageCollections = onManageCollections
                    )
                }

                pagingItems.loadState.refresh is LoadStateError -> {
                    val error = pagingItems.loadState.refresh as LoadStateError
                    ErrorState(
                        message = placesLoadErrorMessage(error.error),
                        onRetry = { pagingItems.retry() }
                    )
                }

                pagingItems.loadState.refresh is LoadStateNotLoading -> {
                    when {
                        // The search that is actually in effect, not the text still in the box -
                        // submitting clears the box, which used to make an empty result read as
                        // "no locations yet, create your first one".
                        pagingItems.itemCount == 0 && actualSearchQuery.isEmpty() && activeFilterCount == 0 -> {
                            EmptyState()
                        }

                        pagingItems.itemCount == 0 -> {
                            NoSearchResultsState(
                                searchQuery = actualSearchQuery,
                                hasFilters = activeFilterCount > 0
                            )
                        }

                        else -> {
                            AdventuresPagingList(
                                pagingItems = pagingItems,
                                view = view,
                                collections = collections,
                                onAdventureClick = onAdventureClick,
                                onEditAdventure = onEditAdventure,
                                onAddPhoto = onAddPhoto,
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
    view: PlacesView,
    collections: List<UltraSlimCollection>,
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    onAddPhoto: (Location) -> Unit = onEditAdventure,
    onDuplicateAdventure: (Location) -> Unit,
    onShareAdventure: (Location) -> Unit,
    onDeleteAdventure: (Location) -> Unit,
    onManageCollections: (Location) -> Unit
) {
    if (view == PlacesView.PHOTOS) {
        PlacesPhotoGrid(
            pagingItems = pagingItems,
            onAdventureClick = onAdventureClick,
            onEditAdventure = onEditAdventure,
            onAddPhoto = onAddPhoto,
            onDuplicateAdventure = onDuplicateAdventure,
            onShareAdventure = onShareAdventure,
            onDeleteAdventure = onDeleteAdventure,
            onManageCollections = onManageCollections,
        )
        return
    }
    // Rows, as the canvas draws Places: a photograph is a thumbnail here and the place's own page
    // is where it is shown large. One column at every width, capped like every other screen; in the
    // two-pane layout the list side is narrower than the cap anyway.
    ContentColumn {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)
        ) {
            items(
                count = pagingItems.itemCount,
                key = pagingItems.itemKey { it.id }
            ) { index ->
                val adventure = pagingItems[index]
                if (adventure != null) {
                    PlaceRow(
                        location = adventure,
                        // One group: the outer corners round, the inner ones barely. A page loaded
                        // below turns the last row into a middle one, which is what it then is.
                        shape = SegmentedListDefaults.shapeFor(index, pagingItems.itemCount),
                        onClick = { onAdventureClick(adventure) },
                        onEdit = { onEditAdventure(adventure) },
                        onAddPhoto = { onAddPhoto(adventure) },
                        onDuplicate = { onDuplicateAdventure(adventure) },
                        onShare = { onShareAdventure(adventure) },
                        onDelete = { onDeleteAdventure(adventure) },
                        onManageCollections = { onManageCollections(adventure) }
                    )
                }
            }

            when (val append = pagingItems.loadState.append) {
                is LoadStateLoading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(modifier = Modifier.size(40.dp))
                    }
                }

                is LoadStateError -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = placesLoadErrorMessage(append.error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

/**
 * The photographs view: each place as the card the list used before the redesign, its photograph
 * across the whole width. One column on a phone; the window decides how many fit on a tablet.
 */
@Composable
private fun PlacesPhotoGrid(
    pagingItems: LazyPagingItems<Location>,
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    onAddPhoto: (Location) -> Unit,
    onDuplicateAdventure: (Location) -> Unit,
    onShareAdventure: (Location) -> Unit,
    onDeleteAdventure: (Location) -> Unit,
    onManageCollections: (Location) -> Unit,
) {
    ContentColumn {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(count = pagingItems.itemCount, key = pagingItems.itemKey { it.id }) { index ->
                val adventure = pagingItems[index]
                if (adventure != null) {
                    AdventureItem(
                        location = adventure,
                        onClick = { onAdventureClick(adventure) },
                        onEdit = { onEditAdventure(adventure) },
                        onAddPhoto = { onAddPhoto(adventure) },
                        onDuplicate = { onDuplicateAdventure(adventure) },
                        onShare = { onShareAdventure(adventure) },
                        onDelete = { onDeleteAdventure(adventure) },
                        onManageCollections = { onManageCollections(adventure) },
                    )
                }
            }
            when (val append = pagingItems.loadState.append) {
                is LoadStateLoading -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        LoadingIndicator(modifier = Modifier.size(40.dp))
                    }
                }
                is LoadStateError -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = placesLoadErrorMessage(append.error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {}
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
                text = "No places yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tap + to add the first one - somewhere you have been, or somewhere you are going.",
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
