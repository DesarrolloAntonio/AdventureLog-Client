package com.desarrollodroide.adventurelog.feature.collections.ui.screens

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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import com.desarrollodroide.adventurelog.core.model.CollectionExport
import com.desarrollodroide.adventurelog.core.model.TripStatus
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import com.desarrollodroide.adventurelog.core.model.CollectionInvite
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionsTab
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionsTabContent
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
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.collections.ui.components.SlimCollectionItem
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionsSortBottomSheet
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.ErrorState
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingCard
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CollectionsScreen(
    onCollectionClick: (String, String) -> Unit = { _, _ -> },
    onAddCollectionClick: () -> Unit = { },
    onEditCollection: (UltraSlimCollection) -> Unit = { },
    onPagingItemsReady: (LazyPagingItems<UltraSlimCollection>) -> Unit = { },
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = koinViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOptions by viewModel.sortOptions.collectAsStateWithLifecycle()
    val showSortSheet by viewModel.showSortSheet.collectAsStateWithLifecycle()
    val pagingItems = viewModel.collectionsPagingData.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val collectionCount by viewModel.collectionCount.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val busyLabel by viewModel.busyLabel.collectAsStateWithLifecycle()
    val tab by viewModel.tab.collectAsStateWithLifecycle()
    val tabContent by viewModel.tabContent.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    var collectionToDelete by remember { mutableStateOf<UltraSlimCollection?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }
    
    // Notify when paging items are ready
    LaunchedEffect(pagingItems) {
        onPagingItemsReady(pagingItems)
    }

    // Show sort bottom sheet
    if (showSortSheet) {
        CollectionsSortBottomSheet(
            sortOptions = sortOptions,
            onSortOptionsChanged = viewModel::onSortOptionsChanged,
            onDismiss = viewModel::hideSortSheet
        )
    }

    CollectionsContent(
        pagingItems = pagingItems,
        searchQuery = searchQuery,
        hasActiveSorting = viewModel.hasActiveSorting(),
        isRefreshing = isRefreshing,
        snackbarHostState = snackbarHostState,
        onCollectionClick = onCollectionClick,
        onAddCollectionClick = onAddCollectionClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onShowSort = viewModel::showSortSheet,
        onEditCollection = onEditCollection,
        onDeleteCollection = { collection -> collectionToDelete = collection },
        onRefresh = {
            viewModel.refresh()
            pagingItems.refresh()
        },
        tab = tab,
        onTabSelected = viewModel::onTabSelected,
        tabContent = tabContent,
        onRespondToInvite = viewModel::respondToInvite,
        onShareCollection = { viewModel.exportCollection(it, CollectionExport.SHARE_CARD) },
        onDuplicateCollection = viewModel::duplicateCollection,
        onArchiveCollection = { viewModel.setArchived(it, !it.isArchived) },
        onDownloadPdf = { viewModel.exportCollection(it, CollectionExport.PDF) },
        onExportZip = { viewModel.exportCollection(it, CollectionExport.ZIP) },
        busyLabel = busyLabel,
        collectionCount = collectionCount,
        statusFilter = statusFilter,
        onStatusFilterChanged = viewModel::onStatusFilterChanged,
        modifier = modifier
    )

    LaunchedEffect(pagingItems.loadState.refresh) {
        if (pagingItems.loadState.refresh is LoadStateNotLoading && isRefreshing) {
            // Refresh completes automatically in the viewModel
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
    pagingItems: LazyPagingItems<UltraSlimCollection>,
    searchQuery: String,
    hasActiveSorting: Boolean,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
    onCollectionClick: (String, String) -> Unit,
    onAddCollectionClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onShowSort: () -> Unit,
    onEditCollection: (UltraSlimCollection) -> Unit,
    onDeleteCollection: (UltraSlimCollection) -> Unit,
    onRefresh: () -> Unit,
    tab: CollectionsTab = CollectionsTab.MINE,
    onTabSelected: (CollectionsTab) -> Unit = {},
    tabContent: CollectionsTabContent = CollectionsTabContent(),
    onRespondToInvite: (CollectionInvite, Boolean) -> Unit = { _, _ -> },
    onShareCollection: (UltraSlimCollection) -> Unit = {},
    onDuplicateCollection: (UltraSlimCollection) -> Unit = {},
    onArchiveCollection: (UltraSlimCollection) -> Unit = {},
    onDownloadPdf: (UltraSlimCollection) -> Unit = {},
    onExportZip: (UltraSlimCollection) -> Unit = {},
    busyLabel: String? = null,
    collectionCount: Int = 0,
    statusFilter: TripStatus? = null,
    onStatusFilterChanged: (TripStatus?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier,
        topBar = {
          Column {
            if (collectionCount > 0) {
                Text(
                    text = "$collectionCount " +
                        if (collectionCount == 1) "collection" else "collections",
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
                    onSearchSubmit = { },
                    placeholder = "Search collections...",
                    showSearchButton = false,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onShowSort,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            tint = if (hasActiveSorting) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        if (hasActiveSorting) {
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd),
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            CollectionsTabRow(selected = tab, onSelect = onTabSelected)

            // The status filter only means anything for the paged list of the user's own
            // collections; the other tabs are short, whole lists.
            if (tab == CollectionsTab.MINE) {
                StatusFilterRow(
                    selected = statusFilter,
                    onSelect = onStatusFilterChanged
                )
            }
          }
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
                // The other three tabs are whole lists from the server, not paged.
                tab != CollectionsTab.MINE -> {
                    TabContentList(
                        tab = tab,
                        content = tabContent,
                        onCollectionClick = onCollectionClick,
                        onRespondToInvite = onRespondToInvite,
                        onEditCollection = onEditCollection,
                        onDeleteCollection = onDeleteCollection,
                        onShareCollection = onShareCollection,
                        onDuplicateCollection = onDuplicateCollection,
                        onArchiveCollection = onArchiveCollection,
                        onDownloadPdf = onDownloadPdf,
                        onExportZip = onExportZip,
                        busyLabel = busyLabel
                    )
                }

                // Show existing content during pull to refresh
                isRefreshing && pagingItems.itemCount > 0 -> {
                    CollectionsPagingList(
                        pagingItems = pagingItems,
                        onCollectionClick = onCollectionClick,
                        onEditCollection = onEditCollection,
                        onDeleteCollection = onDeleteCollection,
                        onShareCollection = onShareCollection,
                        onDuplicateCollection = onDuplicateCollection,
                        onArchiveCollection = onArchiveCollection,
                        onDownloadPdf = onDownloadPdf,
                        onExportZip = onExportZip,
                        busyLabel = busyLabel,
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
                        // "None at all" and "none matching" are different things to say. A
                        // status filter that matches nothing used to read as an empty account.
                        pagingItems.itemCount == 0 &&
                            searchQuery.isEmpty() && statusFilter == null -> {
                            EmptyState()
                        }

                        pagingItems.itemCount == 0 -> {
                            NoSearchResultsState(
                                searchQuery = searchQuery,
                                statusFilter = statusFilter
                            )
                        }

                        else -> {
                            CollectionsPagingList(
                                pagingItems = pagingItems,
                                onCollectionClick = onCollectionClick,
                                onEditCollection = onEditCollection,
                                onDeleteCollection = onDeleteCollection,
                                onShareCollection = onShareCollection,
                                onDuplicateCollection = onDuplicateCollection,
                                onArchiveCollection = onArchiveCollection,
                                onDownloadPdf = onDownloadPdf,
                                onExportZip = onExportZip,
                                busyLabel = busyLabel,
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
    pagingItems: LazyPagingItems<UltraSlimCollection>,
    onCollectionClick: (String, String) -> Unit,
    onEditCollection: (UltraSlimCollection) -> Unit,
    onDeleteCollection: (UltraSlimCollection) -> Unit,
    onShareCollection: (UltraSlimCollection) -> Unit = {},
    onDuplicateCollection: (UltraSlimCollection) -> Unit = {},
    onArchiveCollection: (UltraSlimCollection) -> Unit = {},
    onDownloadPdf: (UltraSlimCollection) -> Unit = {},
    onExportZip: (UltraSlimCollection) -> Unit = {},
    busyLabel: String? = null,
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
                SlimCollectionItem(
                    collection = collection,
                    onClick = { onCollectionClick(collection.id, collection.name) },
                    onEditCollection = { onEditCollection(collection) },
                    onDeleteCollection = { onDeleteCollection(collection) },
                    onShareCollection = { onShareCollection(collection) },
                    onDuplicateCollection = { onDuplicateCollection(collection) },
                    onArchiveCollection = { onArchiveCollection(collection) },
                    onDownloadPdf = { onDownloadPdf(collection) },
                    onExportZip = { onExportZip(collection) },
                    busyLabel = busyLabel
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
                text = "Create your first collection to group the places you visit.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoSearchResultsState(searchQuery: String, statusFilter: TripStatus? = null) {
    val statusLabel = when (statusFilter) {
        TripStatus.FOLDER -> "folders"
        TripStatus.UPCOMING -> "upcoming trips"
        TripStatus.IN_PROGRESS -> "trips in progress"
        TripStatus.COMPLETED -> "completed trips"
        null -> null
    }

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
                    searchQuery.isNotEmpty() && statusLabel != null ->
                        "No $statusLabel matching \"$searchQuery\""
                    searchQuery.isNotEmpty() -> "No results for \"$searchQuery\""
                    statusLabel != null -> "No $statusLabel"
                    else -> "Nothing to show"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "Try searching with different keywords"
                } else {
                    "Nothing here has that status yet"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * The status filter the web keeps in its sidebar. A phone has no sidebar, so it sits under the
 * search field as a scrolling row of chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterRow(
    selected: TripStatus?,
    onSelect: (TripStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        null to "All",
        TripStatus.FOLDER to "📁 Folder",
        TripStatus.UPCOMING to "🚀 Upcoming",
        TripStatus.IN_PROGRESS to "🎯 In progress",
        TripStatus.COMPLETED to "✓ Completed"
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { (status, label) ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelect(status) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun CollectionsTabRow(
    selected: CollectionsTab,
    onSelect: (CollectionsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = CollectionsTab.entries.indexOf(selected),
        modifier = modifier.fillMaxWidth(),
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        divider = {}
    ) {
        CollectionsTab.entries.forEach { entry ->
            Tab(
                selected = selected == entry,
                onClick = { onSelect(entry) },
                text = { Text(entry.label) }
            )
        }
    }
}

/** Whole-list tabs: the archive, what others have shared, and pending invitations. */
@Composable
private fun TabContentList(
    tab: CollectionsTab,
    content: CollectionsTabContent,
    onCollectionClick: (String, String) -> Unit,
    onRespondToInvite: (CollectionInvite, Boolean) -> Unit,
    onEditCollection: (UltraSlimCollection) -> Unit,
    onDeleteCollection: (UltraSlimCollection) -> Unit,
    onShareCollection: (UltraSlimCollection) -> Unit,
    onDuplicateCollection: (UltraSlimCollection) -> Unit,
    onArchiveCollection: (UltraSlimCollection) -> Unit,
    onDownloadPdf: (UltraSlimCollection) -> Unit,
    onExportZip: (UltraSlimCollection) -> Unit,
    busyLabel: String?,
    modifier: Modifier = Modifier
) {
    when {
        content.isLoading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        content.error != null -> Box(
            modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(content.error, textAlign = TextAlign.Center)
        }

        tab == CollectionsTab.INVITES && content.invites.isEmpty() ->
            TabEmpty("No invitations", "Collections other people invite you to appear here.")

        tab != CollectionsTab.INVITES && content.collections.isEmpty() -> TabEmpty(
            title = if (tab == CollectionsTab.ARCHIVED) "Nothing archived" else "Nothing shared",
            body = if (tab == CollectionsTab.ARCHIVED) {
                "Collections you archive are kept here."
            } else {
                "Collections other people share with you appear here."
            }
        )

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (tab == CollectionsTab.INVITES) {
                items(content.invites, key = { it.id }) { invite ->
                    InviteCard(invite = invite, onRespond = onRespondToInvite)
                }
            } else {
                items(content.collections, key = { it.id }) { collection ->
                    // The same actions as the main list: restoring something from the archive is
                    // only reachable from here, so these cannot be left inert.
                    SlimCollectionItem(
                        collection = collection,
                        onClick = { onCollectionClick(collection.id, collection.name) },
                        onEditCollection = { onEditCollection(collection) },
                        onDeleteCollection = { onDeleteCollection(collection) },
                        onShareCollection = { onShareCollection(collection) },
                        onDuplicateCollection = { onDuplicateCollection(collection) },
                        onArchiveCollection = { onArchiveCollection(collection) },
                        onDownloadPdf = { onDownloadPdf(collection) },
                        onExportZip = { onExportZip(collection) },
                        busyLabel = busyLabel
                    )
                }
            }
        }
    }
}

@Composable
private fun TabEmpty(title: String, body: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InviteCard(
    invite: CollectionInvite,
    onRespond: (CollectionInvite, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = invite.collectionName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Shared by ${invite.ownerDisplayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onRespond(invite, false) }) { Text("Decline") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onRespond(invite, true) }) { Text("Accept") }
            }
        }
    }
}
