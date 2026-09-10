package com.desarrollodroide.adventurelog.feature.collections.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionTab
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionView
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionViewSwitcher
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionsTabs
import com.desarrollodroide.adventurelog.feature.collections.ui.state.agenda
import com.desarrollodroide.adventurelog.feature.collections.ui.state.stats
import com.desarrollodroide.adventurelog.feature.collections.ui.views.CollectionCalendarView
import com.desarrollodroide.adventurelog.feature.collections.ui.views.CollectionMapView
import com.desarrollodroide.adventurelog.feature.collections.ui.views.CollectionStatsView
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionDetailViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.DeleteState
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.UpdateCollectionsState
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog
import com.desarrollodroide.adventurelog.feature.ui.components.ManageCollectionsDialog
import com.desarrollodroide.adventurelog.feature.ui.components.ChipTone
import com.desarrollodroide.adventurelog.feature.ui.components.MetaChip
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.DeleteOutline
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.Lodging

@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    onAddTransportation: () -> Unit,
    onEditTransportation: (Transportation) -> Unit,
    onAddNote: (String) -> Unit = {},
    onEditNote: (String, Note) -> Unit = { _, _ -> },
    onAddChecklist: (String) -> Unit = {},
    onEditChecklist: (String, Checklist) -> Unit = { _, _ -> },
    onAddLodging: (String) -> Unit = {},
    onEditLodging: (String, Lodging) -> Unit = { _, _ -> },
    /**
     * Whether the screen has to say where it is. Reached from Home it does not: that route puts a
     * breadcrumb in the shell's app bar. Reached from the Collections tab it does - that pane
     * keeps its own back stack, so the shell's route never changes and its breadcrumb never
     * appears. Without this the screen opened on a description, with no name on it and no way
     * back but the system gesture.
     */
    showTitle: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: CollectionDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedView by viewModel.selectedView.collectAsStateWithLifecycle()
    val allCollections by viewModel.allCollections.collectAsStateWithLifecycle()
    val collectionsLoading by viewModel.collectionsLoading.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val updateCollectionsState by viewModel.updateCollectionsState.collectAsStateWithLifecycle()
    
    var locationToManageCollections by remember { mutableStateOf<Location?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(collectionId) {
        viewModel.loadCollection(collectionId)
    }
    
    // Handle delete state changes
    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is DeleteState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearDeleteState()
            }
            is DeleteState.Error -> {
                snackbarHostState.showSnackbar("Error: ${state.message}")
                viewModel.clearDeleteState()
            }
            else -> {}
        }
    }
    
    // Handle update collections state changes
    LaunchedEffect(updateCollectionsState) {
        when (val state = updateCollectionsState) {
            is UpdateCollectionsState.Success -> {
                snackbarHostState.showSnackbar("Collections updated successfully")
                viewModel.clearUpdateCollectionsState()
            }
            is UpdateCollectionsState.Error -> {
                snackbarHostState.showSnackbar("Error updating collections: ${state.message}")
                viewModel.clearUpdateCollectionsState()
            }
            else -> {}
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                LoadingDialog(
                    isLoading = true,
                    showMessage = false
                )
            }
            uiState.errorMessage != null -> {
                Text(
                    text = "Error: ${uiState.errorMessage}",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
            uiState.collection != null -> {
                CollectionDetailContent(
                    collection = uiState.collection!!,
                    showTitle = showTitle,
                    onBackClick = onBackClick,
                    onHomeClick = onHomeClick,
                    selectedTab = selectedTab,
                    onTabSelected = viewModel::onTabSelected,
                    selectedView = selectedView,
                    onViewSelected = viewModel::onViewSelected,
                    onAdventureClick = onAdventureClick,
                    onEditAdventure = onEditAdventure,
                    onDeleteAdventure = { adventure -> 
                        viewModel.deleteAdventure(adventure.id)
                    },
                    onManageCollections = { adventure -> 
                        locationToManageCollections = adventure 
                    },
                    onAddTransportation = onAddTransportation,
                    onEditTransportation = onEditTransportation,
                    onDeleteTransportation = { transportation ->
                        viewModel.deleteTransportation(transportation.id)
                    },
                    onAddNote = { onAddNote(collectionId) },
                    onEditNote = { note -> onEditNote(collectionId, note) },
                    onDeleteNote = { note -> viewModel.deleteNote(note.id) },
                    onAddChecklist = { onAddChecklist(collectionId) },
                    onEditChecklist = { list -> onEditChecklist(collectionId, list) },
                    onDeleteChecklist = { list -> viewModel.deleteChecklist(list.id) },
                    onAddLodging = { onAddLodging(collectionId) },
                    onEditLodging = { stay -> onEditLodging(collectionId, stay) },
                    onDeleteLodging = { stay -> viewModel.deleteLodging(stay.id) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    
    // Manage Collections dialog
    locationToManageCollections?.let { adventure ->
        ManageCollectionsDialog(
            location = adventure,
            allCollections = allCollections,
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

@Composable
fun CollectionDetailContent(
    collection: Collection,
    showTitle: Boolean = false,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    selectedTab: CollectionTab,
    onTabSelected: (CollectionTab) -> Unit,
    selectedView: CollectionView = CollectionView.ITEMS,
    onViewSelected: (CollectionView) -> Unit = {},
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    onDeleteAdventure: (Location) -> Unit,
    onManageCollections: (Location) -> Unit,
    onAddTransportation: () -> Unit,
    onEditTransportation: (Transportation) -> Unit,
    onDeleteTransportation: (Transportation) -> Unit,
    onAddNote: () -> Unit = {},
    onEditNote: (Note) -> Unit = {},
    onDeleteNote: (Note) -> Unit = {},
    onAddChecklist: () -> Unit = {},
    onEditChecklist: (Checklist) -> Unit = {},
    onDeleteChecklist: (Checklist) -> Unit = {},
    onAddLodging: () -> Unit = {},
    onEditLodging: (Lodging) -> Unit = {},
    onDeleteLodging: (Lodging) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            CollectionHeader(
                collection,
                showTitle = showTitle,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick
            )
        }
        
        item {
            // No inset of its own: the list already pads, and 16 on top of 16 made this the one
            // element on the screen narrower than everything around it. The extra space above is
            // the break between what the collection is and how you move around inside it -
            // without it the tabs read as a fourth row of the header's chips.
            CollectionViewSwitcher(
                modifier = Modifier.padding(top = 8.dp),
                selectedView = selectedView,
                onViewSelected = onViewSelected,
                // Itinerary is the one view backed by its own server resource
                // (/api/itineraries/) rather than by the collection's own contents, and it is
                // not built yet. Offering the chip and showing an empty page would be worse
                // than not offering it.
                views = CollectionView.entries - CollectionView.ITINERARY
            )
        }

        // The item tabs belong to the Items view and only to it: they choose which of a
        // collection's things to list, which is a question the map and the figures do not ask.
        if (selectedView == CollectionView.ITEMS) {
            item {
                CollectionsTabs(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    counts = mapOf(
                        CollectionTab.LOCATIONS to collection.locations.size,
                        CollectionTab.TRANSPORTATIONS to collection.transportations.size,
                        CollectionTab.LODGING to collection.lodging.size,
                        CollectionTab.NOTES to collection.notes.size,
                        CollectionTab.CHECKLISTS to collection.checklists.size
                    )
                )
            }
        }

        when (selectedView) {
            CollectionView.STATS -> item { CollectionStatsView(stats = collection.stats()) }
            CollectionView.CALENDAR -> item { CollectionCalendarView(days = collection.agenda()) }
            CollectionView.MAP -> item {
                CollectionMapView(
                    locations = collection.locations,
                    onLocationClick = { id ->
                        collection.locations.find { it.id == id }?.let(onAdventureClick)
                    }
                )
            }
            CollectionView.ITEMS, CollectionView.ITINERARY -> Unit
        }

        if (selectedView == CollectionView.ITEMS) when (selectedTab) {
            CollectionTab.ALL -> {
                // Show Locations section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Places",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "${collection.locations.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                if (collection.locations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "No adventures yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Start adding adventures to build your collection",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(collection.locations) { adventure ->
                        AdventureItem(
                            location = adventure,
                            onClick = { onAdventureClick(adventure) },
                            onEdit = { onEditAdventure(adventure) },
                            onDelete = { onDeleteAdventure(adventure) },
                            onManageCollections = { onManageCollections(adventure) }
                        )
                    }
                }
                
                // Show Transportations section
                val transportations = collection.transportations.map { transportation ->
                    TransportationItem(
                        id = transportation.id,
                        name = transportation.name,
                        type = transportation.type,
                        imageUrl = transportation.images?.find { it.isPrimary }?.image 
                            ?: transportation.images?.firstOrNull()?.image,
                        isNotInItineraryDateRange = isTransportationOutOfRange(
                            transportation = transportation,
                            collectionStartDate = collection.startDate,
                            collectionEndDate = collection.endDate
                        )
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transportations",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onAddTransportation) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add transportation"
                            )
                        }
                    }
                }

                if (transportations.isNotEmpty()) {
                    
                    items(transportations) { transportation ->
                        TransportationItemCard(
                            transportation = transportation,
                            onEdit = { 
                                val originalTransportation = collection.transportations.find { it.id == transportation.id }
                                originalTransportation?.let { onEditTransportation(it) }
                            },
                            onDelete = { 
                                val originalTransportation = collection.transportations.find { it.id == transportation.id }
                                originalTransportation?.let { onDeleteTransportation(it) }
                            }
                        )
                    }
                }
            }
            
            CollectionTab.LOCATIONS -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Places",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "${collection.locations.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                if (collection.locations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "No adventures yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Start adding adventures to build your collection",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(collection.locations) { adventure ->
                        AdventureItem(
                            location = adventure,
                            onClick = { onAdventureClick(adventure) },
                            onEdit = { onEditAdventure(adventure) },
                            onDelete = { onDeleteAdventure(adventure) },
                            onManageCollections = { onManageCollections(adventure) }
                        )
                    }
                }
            }
            
            CollectionTab.TRANSPORTATIONS -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transportations",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onAddTransportation) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add transportation"
                            )
                        }
                    }
                }
                
                val transportations = collection.transportations.map { transportation ->
                    TransportationItem(
                        id = transportation.id,
                        name = transportation.name,
                        type = transportation.type,
                        imageUrl = transportation.images?.find { it.isPrimary }?.image 
                            ?: transportation.images?.firstOrNull()?.image,
                        isNotInItineraryDateRange = isTransportationOutOfRange(
                            transportation = transportation,
                            collectionStartDate = collection.startDate,
                            collectionEndDate = collection.endDate
                        )
                    )
                }
                
                if (transportations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBoat,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "No transportations yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Start adding transportation methods to your collection",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(transportations) { transportation ->
                        TransportationItemCard(
                            transportation = transportation,
                            onEdit = { 
                                val originalTransportation = collection.transportations.find { it.id == transportation.id }
                                originalTransportation?.let { onEditTransportation(it) }
                            },
                            onDelete = { 
                                val originalTransportation = collection.transportations.find { it.id == transportation.id }
                                originalTransportation?.let { onDeleteTransportation(it) }
                            }
                        )
                    }
                }
            }
            
            CollectionTab.LODGING -> {
                item { TabHeading("Lodging", onAdd = onAddLodging) }
                if (collection.lodging.isEmpty()) {
                    item { EmptyTab("No lodging in this collection yet.") }
                } else {
                    items(collection.lodging, key = { it.id }) { stay ->
                        SimpleEntryCard(
                            onClick = { onEditLodging(stay) },
                            onDelete = { onDeleteLodging(stay) },
                            title = stay.name,
                            lines = listOfNotNull(
                                stay.location?.takeIf { it.isNotBlank() },
                                lodgingDates(stay.checkIn, stay.checkOut),
                                stay.reservationNumber?.takeIf { it.isNotBlank() }
                                    ?.let { "Reservation $it" }
                            ),
                            badge = stay.type.replaceFirstChar { c -> c.uppercase() }
                        )
                    }
                }
            }

            CollectionTab.NOTES -> {
                item { TabHeading("Notes", onAdd = onAddNote) }
                if (collection.notes.isEmpty()) {
                    item { EmptyTab("No notes in this collection yet.") }
                } else {
                    items(collection.notes, key = { it.id }) { note ->
                        SimpleEntryCard(
                            title = note.name,
                            lines = listOfNotNull(
                                note.content?.takeIf { it.isNotBlank() },
                                note.date?.substringBefore('T')?.takeIf { it.isNotBlank() }
                            ),
                            badge = null,
                            onClick = { onEditNote(note) },
                            onDelete = { onDeleteNote(note) }
                        )
                    }
                }
            }

            CollectionTab.CHECKLISTS -> {
                item { TabHeading("Checklists", onAdd = onAddChecklist) }
                if (collection.checklists.isEmpty()) {
                    item { EmptyTab("No checklists in this collection yet.") }
                } else {
                    items(collection.checklists, key = { it.id }) { checklist ->
                        val done = checklist.items.count { it.isChecked }
                        SimpleEntryCard(
                            onClick = { onEditChecklist(checklist) },
                            onDelete = { onDeleteChecklist(checklist) },
                            title = checklist.name,
                            lines = checklist.items.take(4).map { item ->
                                (if (item.isChecked) "\u2713 " else "\u25cb ") + item.name
                            } + listOfNotNull(
                                "and ${checklist.items.size - 4} more"
                                    .takeIf { checklist.items.size > 4 }
                            ),
                            badge = if (checklist.items.isEmpty()) {
                                null
                            } else {
                                "$done / ${checklist.items.size}"
                            }
                        )
                    }
                }
            }

            else -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Coming Soon",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${selectedTab.title} functionality will be available in a future update",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionHeader(
    collection: Collection,
    modifier: Modifier = Modifier,
    showTitle: Boolean = false,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showTitle) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onBackClick)
                        .padding(4.dp)
                )
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onHomeClick)
                        .padding(4.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(12.dp)
                )
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Description (only if not blank)
        if (collection.description.isNotBlank()) {
            Text(
                text = collection.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // The two facts about the collection itself, in the same chips the rest of the app uses.
        // These were the last outlined AssistChips left standing.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetaChip(
                text = if (collection.isPublic) "\uD83C\uDF0D Public" else "\uD83D\uDD12 Private",
                tone = if (collection.isPublic) ChipTone.NEUTRAL else ChipTone.WARNING
            )
            MetaChip(
                text = if (collection.isArchived) "\uD83D\uDCE6 Archived" else "\u2705 Active",
                tone = if (collection.isArchived) ChipTone.NEUTRAL else ChipTone.POSITIVE
            )
        }
    }
}

data class TransportationItem(
    val id: String,
    val name: String,
    val type: String,
    val imageUrl: String?,
    val isNotInItineraryDateRange: Boolean = false
)

@Composable
fun TransportationItemCard(
    transportation: TransportationItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imageLoader = LocalImageLoader.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        onClick = onEdit,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box {
            val hasImage = transportation.imageUrl?.isNotEmpty() == true
            
            if (hasImage) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = transportation.imageUrl,
                        imageLoader = imageLoader
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                EmptyTransportationDesign(
                    transportationName = transportation.name,
                    transportationType = transportation.type,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = transportation.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Type tag
                    MetaChip(
                        text = getTransportationIcon(transportation.type) + " " +
                            transportation.type.replaceFirstChar { it.uppercase() },
                        tone = ChipTone.ACCENT
                    )

                    // Not in itinerary date range tag
                    if (transportation.isNotInItineraryDateRange) {
                        MetaChip(
                            text = "Not in itinerary date range",
                            tone = ChipTone.WARNING
                        )
                    }
                }
            }

            // Menu button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    IconButton(
                        onClick = { showDropdownMenu = true },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Transportation") },
                        onClick = {
                            onEdit()
                            showDropdownMenu = false
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete",
                                color = Color(0xFFFF3B30)
                            )
                        },
                        onClick = {
                            showDeleteDialog = true
                            showDropdownMenu = false
                        }
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transportation") },
            text = { Text("Are you sure you want to delete \"${transportation.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF3B30))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmptyTransportationDesign(
    transportationName: String,
    transportationType: String,
    modifier: Modifier = Modifier
) {
    val designIndex = remember(transportationName) { 
        kotlin.math.abs(transportationName.hashCode() % 8)
    }
    
    val (backgroundColor, accentColor) = when (designIndex) {
        0 -> Color(0xFFE8EAF6) to Color(0xFF5C6BC0) // Indigo
        1 -> Color(0xFFE0F2F1) to Color(0xFF26A69A) // Teal
        2 -> Color(0xFFFFF3E0) to Color(0xFFFF9800) // Orange
        3 -> Color(0xFFF3E5F5) to Color(0xFF9C27B0) // Purple
        4 -> Color(0xFFE8F5E9) to Color(0xFF4CAF50) // Green
        5 -> Color(0xFFFFEBEE) to Color(0xFFEF5350) // Red
        6 -> Color(0xFFF1F8E9) to Color(0xFF689F38) // Light Green
        else -> Color(0xFFFAFAFA) to Color(0xFF607D8B) // Blue Grey
    }
    
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor,
                            backgroundColor.copy(alpha = 0.7f),
                            Color.White
                        )
                    )
                )
        )
        
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val gridSize = 100.dp.toPx()
            val gridAlpha = 0.03f
            
            var x = gridSize
            while (x < size.width) {
                drawLine(
                    color = accentColor.copy(alpha = gridAlpha),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                x += gridSize
            }
            
            var y = gridSize
            while (y < size.height) {
                drawLine(
                    color = accentColor.copy(alpha = gridAlpha),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += gridSize
            }
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 30.dp)
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = accentColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    )
                    
                    Text(
                        text = getTransportationIcon(transportationType),
                        fontSize = 40.sp,
                        color = accentColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun getTransportationIcon(type: String): String {
    return when (type.lowercase()) {
        "boat", "ship", "ferry" -> "⛵"
        "plane", "flight", "airplane" -> "✈️"
        "car", "automobile" -> "🚗"
        "train" -> "🚆"
        "bus" -> "🚌"
        "bicycle", "bike" -> "🚴"
        "motorcycle" -> "🏍️"
        "taxi" -> "🚕"
        "subway", "metro" -> "🚇"
        else -> "🚗"
    }
}

private fun isTransportationOutOfRange(
    transportation: Transportation,
    collectionStartDate: String?,
    collectionEndDate: String?
): Boolean {
    // If collection doesn't have date range, consider everything in range
    if (collectionStartDate == null || collectionEndDate == null) {
        return false
    }
    
    // Check if transportation is outside collection date range
    return !isTransportationInCollectionDateRange(transportation, collectionStartDate, collectionEndDate)
}

/**
 * Checks if a transportation falls within a collection's date range
 * Based on AdventureLog's frontend dateUtils.ts logic
 */
private fun isTransportationInCollectionDateRange(
    transportation: Transportation,
    collectionStartDate: String,
    collectionEndDate: String
): Boolean {
    return try {
        // Get transportation date range
        val transportationStart = transportation.date
        val transportationEnd = transportation.endDate ?: transportation.date
        
        // If transportation doesn't have dates, consider it in range
        if (transportationStart == null) {
            return true
        }
        
        // Check if dates are all-day (no time portion)
        val transportationStartIsAllDay = isAllDay(transportationStart)
        val transportationEndIsAllDay = transportationEnd?.let { isAllDay(it) } ?: transportationStartIsAllDay
        val collectionStartIsAllDay = isAllDay(collectionStartDate)
        val collectionEndIsAllDay = isAllDay(collectionEndDate)
        
        // If any date is all-day, compare only date portions
        if (transportationStartIsAllDay || transportationEndIsAllDay || collectionStartIsAllDay || collectionEndIsAllDay) {
            val entStartDate = extractDatePortion(transportationStart)
            val entEndDate = extractDatePortion(transportationEnd ?: transportationStart)
            val colStartDate = extractDatePortion(collectionStartDate)
            val colEndDate = extractDatePortion(collectionEndDate)
            
            // Check if date ranges overlap
            return entStartDate <= colEndDate && entEndDate >= colStartDate
        } else {
            // Compare actual datetimes (simplified comparison)
            val entStart = parseDateTimeToComparable(transportationStart)
            val entEnd = parseDateTimeToComparable(transportationEnd ?: transportationStart)
            val colStart = parseDateTimeToComparable(collectionStartDate)
            val colEnd = parseDateTimeToComparable(collectionEndDate)
            
            // Check if datetime ranges overlap
            return entStart <= colEnd && entEnd >= colStart
        }
    } catch (e: Exception) {
        // If parsing fails, consider it in range
        true
    }
}

/**
 * Checks if a date string represents an all-day event (no time portion)
 */
private fun isAllDay(dateString: String?): Boolean {
    return dateString?.length == 10 // YYYY-MM-DD format
}

/**
 * Extracts date portion from a date string for comparison
 */
private fun extractDatePortion(dateString: String): Int {
    return try {
        val datePart = if (dateString.length > 10) dateString.substring(0, 10) else dateString
        val parts = datePart.split("-")
        if (parts.size >= 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            year * 10000 + month * 100 + day
        } else {
            0
        }
    } catch (e: Exception) {
        0
    }
}

private fun parseDateTimeToComparable(dateString: String): Long {
    return try {
        val cleanDate = dateString.replace("T", "").replace(":", "").replace("-", "").replace("Z", "")
        cleanDate.toLongOrNull() ?: 0L
    } catch (e: Exception) {
        0L
    }
}


/** The heading each tab opens with, matching the one the Places tab already had. */
@Composable
private fun TabHeading(title: String, onAdd: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (onAdd != null) {
            IconButton(onClick = onAdd) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add $title")
            }
        }
    }
}

@Composable
private fun EmptyTab(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(28.dp)
        )
    }
}

/**
 * One entry in the lodging, notes or checklists tab.
 *
 * These three were greyed out because the collection model kept only their ids - the server had
 * been sending the whole objects all along and the mapper reduced each to `it.id`.
 */
@Composable
private fun SimpleEntryCard(
    title: String,
    lines: List<String>,
    badge: String?,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (badge != null) {
                    Spacer(Modifier.width(10.dp))
                    MetaChip(text = badge, tone = ChipTone.NEUTRAL)
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            lines.forEach { line ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** "12/09 - 15/09", or one of the two when only one is set. */
private fun lodgingDates(checkIn: String?, checkOut: String?): String? {
    val short = { d: String? ->
        d?.substringBefore('T')?.split("-")?.takeIf { it.size == 3 }?.let { "${it[2]}/${it[1]}" }
    }
    val a = short(checkIn)
    val b = short(checkOut)
    return when {
        a != null && b != null && a != b -> "$a \u2013 $b"
        a != null -> a
        b != null -> b
        else -> null
    }
}
