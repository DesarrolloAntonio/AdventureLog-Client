package com.desarrollodroide.adventurelog.feature.collections.ui.screens

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import com.desarrollodroide.adventurelog.feature.collections.ui.components.ticketStub
import androidx.compose.foundation.lazy.itemsIndexed
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.components.PlaceRow
import androidx.compose.foundation.layout.navigationBarsPadding
import com.desarrollodroide.adventurelog.feature.collections.ui.components.AddToCollectionMenu
import com.desarrollodroide.adventurelog.feature.ui.components.HideShellChrome
import com.desarrollodroide.adventurelog.feature.ui.components.LocalShellActionHost
import androidx.compose.runtime.remember
import kotlin.time.ExperimentalTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDate
import com.desarrollodroide.adventurelog.feature.collections.ui.components.collectionMoment
import com.desarrollodroide.adventurelog.feature.collections.ui.components.arrowDates
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Surface
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.BorderStroke
import com.desarrollodroide.adventurelog.core.model.ownedBy
import com.desarrollodroide.adventurelog.feature.ui.session.rememberCurrentUserId
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
import androidx.compose.foundation.lazy.LazyListScope
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
import com.desarrollodroide.adventurelog.core.model.Recommendation
import com.desarrollodroide.adventurelog.core.model.RecommendationCategory
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionTab
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionView
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionViewSwitcher
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionsTabs
import com.desarrollodroide.adventurelog.feature.collections.ui.components.ItineraryItemPicker
import com.desarrollodroide.adventurelog.feature.collections.ui.state.summary
import com.desarrollodroide.adventurelog.feature.collections.ui.state.agenda
import com.desarrollodroide.adventurelog.feature.collections.ui.state.itinerary
import com.desarrollodroide.adventurelog.feature.collections.ui.state.stats
import com.desarrollodroide.adventurelog.feature.collections.ui.views.CollectionCalendarView
import com.desarrollodroide.adventurelog.feature.collections.ui.views.CollectionItineraryView
import com.desarrollodroide.adventurelog.feature.collections.ui.views.CollectionRecommendationsView
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.RecommendationsUiState
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.RecommendationsViewModel
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
import com.desarrollodroide.adventurelog.feature.collections.ui.components.ticketDateRange

@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    onAddPhoto: (Location) -> Unit = onEditAdventure,
    /** A new place, created inside this collection (QA CO-19). */
    onAddPlace: () -> Unit = {},
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
    /** False in a pane beside the collections list: Back there would point at a list still on screen. */
    showBack: Boolean = true,
    /** Opens the collection's editor from the header. */
    onEditCollection: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: CollectionDetailViewModel = koinViewModel(),
    recommendationsViewModel: RecommendationsViewModel = koinViewModel()
) {
    // On a phone inside the shell the collection brings its own header (Back, Edit) and its own
    // "+", as the canvas draws it; the shell's bars and breadcrumb step aside.
    val onPhoneInShell = LocalShellActionHost.current != null
    if (onPhoneInShell) HideShellChrome()
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedView by viewModel.selectedView.collectAsStateWithLifecycle()
    val itineraryWorking by viewModel.itineraryWorking.collectAsStateWithLifecycle()
    val itineraryTarget by viewModel.itineraryTarget.collectAsStateWithLifecycle()
    val recommendations by recommendationsViewModel.uiState.collectAsStateWithLifecycle()
    val allCollections by viewModel.allCollections.collectAsStateWithLifecycle()
    val collectionsLoading by viewModel.collectionsLoading.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val updateCollectionsState by viewModel.updateCollectionsState.collectAsStateWithLifecycle()
    
    var locationToManageCollections by remember { mutableStateOf<Location?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = rememberCurrentUserId()
    
    LaunchedEffect(collectionId) {
        viewModel.loadCollection(collectionId)
    }
    
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
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
                    currentUserId = currentUserId,
                    collection = uiState.collection!!,
                    showTitle = showTitle || onPhoneInShell,
                    showBack = showBack,
                    onBackClick = onBackClick,
                    onHomeClick = onHomeClick,
                    onEditCollection = onEditCollection,
                    selectedTab = selectedTab,
                    onTabSelected = viewModel::onTabSelected,
                    selectedView = selectedView,
                    onViewSelected = viewModel::onViewSelected,
                    itineraryWorking = itineraryWorking,
                    onAutoGenerateItinerary = viewModel::autoGenerateItinerary,
                    onAddToItineraryDay = viewModel::openItineraryPicker,
                    onRemoveFromItinerary = viewModel::removeFromItinerary,
                    recommendations = recommendations,
                    onRecommendationAnchor = recommendationsViewModel::onAnchorSelected,
                    onRecommendationQuery = recommendationsViewModel::onQueryChanged,
                    onRecommendationCategory = recommendationsViewModel::onCategorySelected,
                    onRecommendationRadius = recommendationsViewModel::onRadiusSelected,
                    onRecommendationSearch = { anchor ->
                        recommendationsViewModel.search(anchor)
                    },
                    onRecommendationAdd = { recommendation ->
                        recommendationsViewModel.addAsPlace(
                            recommendation = recommendation,
                            collectionId = collectionId
                        ) { viewModel.loadCollection(collectionId) }
                    },
                    onAdventureClick = onAdventureClick,
                    onEditAdventure = onEditAdventure,
                    onAddPhoto = onAddPhoto,
                    onDeleteAdventure = { adventure -> 
                        viewModel.deleteAdventure(adventure.id)
                    },
                    onManageCollections = { adventure -> 
                        locationToManageCollections = adventure 
                    },
                    onDuplicateAdventure = viewModel::duplicateLocation,
                    onShareAdventure = viewModel::shareLocation,
                    onRemoveFromCollection = { adventure -> viewModel.removeFromCollection(adventure, collectionId) },
                    onAddPlace = onAddPlace,
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
        
        // The canvas's one "+": a menu of the five kinds, while the collection is a list of them.
        if (uiState.collection != null && selectedView == CollectionView.ITEMS) {
            AddToCollectionMenu(
                onAddPlace = onAddPlace,
                onAddTransportation = onAddTransportation,
                onAddLodging = { onAddLodging(collectionId) },
                onAddNote = { onAddNote(collectionId) },
                onAddChecklist = { onAddChecklist(collectionId) },
                modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding()
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    
    itineraryTarget?.let { target ->
        uiState.collection?.let { collection ->
            ItineraryItemPicker(
                collection = collection,
                dayLabel = target.label,
                onPick = { kind, itemId ->
                    viewModel.addToItinerary(kind, itemId, target.date)
                },
                onDismiss = viewModel::dismissItineraryPicker
            )
        }
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
    showBack: Boolean = true,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onEditCollection: (() -> Unit)? = null,
    selectedTab: CollectionTab,
    onTabSelected: (CollectionTab) -> Unit,
    selectedView: CollectionView = CollectionView.ITEMS,
    onViewSelected: (CollectionView) -> Unit = {},
    itineraryWorking: Boolean = false,
    onAutoGenerateItinerary: () -> Unit = {},
    onAddToItineraryDay: (String?, String) -> Unit = { _, _ -> },
    onRemoveFromItinerary: (String) -> Unit = {},
    recommendations: RecommendationsUiState = RecommendationsUiState(),
    onRecommendationAnchor: (String?) -> Unit = {},
    onRecommendationQuery: (String) -> Unit = {},
    onRecommendationCategory: (RecommendationCategory) -> Unit = {},
    onRecommendationRadius: (Int) -> Unit = {},
    onRecommendationSearch: (Pair<Double, Double>?) -> Unit = {},
    onRecommendationAdd: (Recommendation) -> Unit = {},
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    onAddPhoto: (Location) -> Unit = onEditAdventure,
    onDeleteAdventure: (Location) -> Unit,
    onManageCollections: (Location) -> Unit,
    onDuplicateAdventure: (Location) -> Unit = {},
    onShareAdventure: (Location) -> Unit = {},
    onRemoveFromCollection: (Location) -> Unit = {},
    onAddPlace: () -> Unit = {},
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
    /** Tells your places from the owner's in a collection shared with you (QA 09, MC-02). */
    currentUserId: String? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            // Room for the "+" menu over the end of the list.
            bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            CollectionHeader(
                collection,
                showTitle = showTitle,
                showBack = showBack,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onEditCollection = onEditCollection
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
                views = CollectionView.entries,
                bleed = 16.dp,
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
            CollectionView.ITINERARY -> item {
                CollectionItineraryView(
                    itinerary = collection.itinerary(),
                    isWorking = itineraryWorking,
                    onAutoGenerate = onAutoGenerateItinerary,
                    onAddToDay = onAddToItineraryDay,
                    onRemoveEntry = onRemoveFromItinerary
                )
            }
            CollectionView.RECOMMENDATIONS -> item {
                // Only places with coordinates can anchor a search; one without them would send
                // the server nothing to look around.
                val anchors = collection.locations.filter {
                    !it.latitude.isNullOrBlank() && !it.longitude.isNullOrBlank()
                }
                CollectionRecommendationsView(
                    state = recommendations,
                    anchors = anchors,
                    onAnchorSelected = onRecommendationAnchor,
                    onQueryChanged = onRecommendationQuery,
                    onCategorySelected = onRecommendationCategory,
                    onRadiusSelected = onRecommendationRadius,
                    onSearch = {
                        val anchor = anchors
                            .firstOrNull { it.id == recommendations.anchorLocationId }
                            ?.let { place ->
                                val lat = place.latitude?.toDoubleOrNull()
                                val lon = place.longitude?.toDoubleOrNull()
                                if (lat != null && lon != null) lat to lon else null
                            }
                        onRecommendationSearch(anchor)
                    },
                    onAdd = onRecommendationAdd
                )
            }
            CollectionView.ITEMS -> Unit
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
                        
                        // A way to add a place here, like every other section. It showed a count bubble
                        // instead - the chip above already says how many (QA CO-19).
                        IconButton(onClick = onAddPlace) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add place"
                            )
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
                                    text = "No places in this collection yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap + to add a new one, or add a place you already have from its menu in Places.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    item {
                        // Rows, as in Places and as the canvas draws a collection's places: one group,
                        // split by the thin gap rather than the list's 24dp.
                        Column(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
                            collection.locations.forEachIndexed { index, adventure ->
                            PlaceRow(
                                location = adventure,
                                shape = SegmentedListDefaults.shapeFor(index, collection.locations.size),
                                onClick = { onAdventureClick(adventure) },
                                onEdit = { onEditAdventure(adventure) },
                                onAddPhoto = { onAddPhoto(adventure) },
                                onDelete = { onDeleteAdventure(adventure) },
                                onManageCollections = { onManageCollections(adventure) },
                                onDuplicate = { onDuplicateAdventure(adventure) },
                                onShare = { onShareAdventure(adventure) },
                                onRemoveFromCollection = { onRemoveFromCollection(adventure) },
                                isOwner = ownedBy(adventure.user.uuid, currentUserId)
                                )
                            }
                        }
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
                        ),
                        summary = transportation.summary()
                    )
                }
                
                // In All, a kind shows only when the trip has some: an empty "Transport" heading
                // sat at the end of every trip without journeys, while Lodging, Notes and
                // Checklists were left out of All altogether.
                if (transportations.isNotEmpty()) {
                    item { TabHeading("Transport", onAdd = onAddTransportation) }
                    
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

                if (collection.lodging.isNotEmpty()) {
                    item { TabHeading("Lodging", onAdd = onAddLodging) }
                    lodgingItems(collection.lodging, onEditLodging, onDeleteLodging)
                }
                if (collection.notes.isNotEmpty()) {
                    item { TabHeading("Notes", onAdd = onAddNote) }
                    noteItems(collection.notes, onEditNote, onDeleteNote)
                }
                if (collection.checklists.isNotEmpty()) {
                    item { TabHeading("Checklists", onAdd = onAddChecklist) }
                    checklistItems(collection.checklists, onEditChecklist, onDeleteChecklist)
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
                        
                        // A way to add a place here, like every other section. It showed a count bubble
                        // instead - the chip above already says how many (QA CO-19).
                        IconButton(onClick = onAddPlace) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add place"
                            )
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
                                    text = "No places in this collection yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap + to add a new one, or add a place you already have from its menu in Places.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    item {
                        // Rows, as in Places and as the canvas draws a collection's places: one group,
                        // split by the thin gap rather than the list's 24dp.
                        Column(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
                            collection.locations.forEachIndexed { index, adventure ->
                            PlaceRow(
                                location = adventure,
                                shape = SegmentedListDefaults.shapeFor(index, collection.locations.size),
                                onClick = { onAdventureClick(adventure) },
                                onEdit = { onEditAdventure(adventure) },
                                onAddPhoto = { onAddPhoto(adventure) },
                                onDelete = { onDeleteAdventure(adventure) },
                                onManageCollections = { onManageCollections(adventure) },
                                onDuplicate = { onDuplicateAdventure(adventure) },
                                onShare = { onShareAdventure(adventure) },
                                onRemoveFromCollection = { onRemoveFromCollection(adventure) },
                                isOwner = ownedBy(adventure.user.uuid, currentUserId)
                                )
                            }
                        }
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
                            text = "Transport",
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
                        ),
                        summary = transportation.summary()
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
                    lodgingItems(collection.lodging, onEditLodging, onDeleteLodging)
                }
            }

            CollectionTab.NOTES -> {
                item { TabHeading("Notes", onAdd = onAddNote) }
                if (collection.notes.isEmpty()) {
                    item { EmptyTab("No notes in this collection yet.") }
                } else {
                    noteItems(collection.notes, onEditNote, onDeleteNote)
                }
            }

            CollectionTab.CHECKLISTS -> {
                item { TabHeading("Checklists", onAdd = onAddChecklist) }
                if (collection.checklists.isEmpty()) {
                    item { EmptyTab("No checklists in this collection yet.") }
                } else {
                    checklistItems(collection.checklists, onEditChecklist, onDeleteChecklist)
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

@OptIn(ExperimentalTime::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionHeader(
    collection: Collection,
    modifier: Modifier = Modifier,
    showTitle: Boolean = false,
    showBack: Boolean = true,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    /** Opens the collection's editor; null where there is nowhere to edit it from. */
    onEditCollection: (() -> Unit)? = null,
    today: LocalDate = remember {
        kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    },
) {
    val colors = MaterialTheme.colorScheme
    val moment = collectionMoment(collection.startDate, collection.endDate, today)
    val dates = arrowDates(collection.startDate, collection.endDate)
    // The list worth watching before a trip: the first one with anything on it.
    val checklist = collection.checklists.firstOrNull { it.items.isNotEmpty() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if ((showTitle && showBack) || onEditCollection != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showTitle && showBack) {
                    FilledIconButton(
                        onClick = onBackClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = colors.surfaceContainerLowest,
                            contentColor = colors.onSurface
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Spacer(Modifier.weight(1f))
                if (onEditCollection != null) {
                    FilledTonalIconButton(onClick = onEditCollection) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit collection")
                    }
                }
            }
        }

        moment.eyebrow?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = colors.primary
            )
        }
        Text(text = collection.name, style = AdventureType.display(58.sp), maxLines = 3, overflow = TextOverflow.Ellipsis)

        // The ticket, as the canvas draws it: a stub with one big figure, a torn edge, then the
        // dates and the packing list filling up.
        val stub = ticketStub(collection.startDate, collection.endDate, today, collection.locations.size)
        val background = colors.background
        val edge = colors.outlineVariant
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colors.surfaceContainerLowest,
            border = BorderStroke(1.dp, edge),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier.width(116.dp).padding(start = 20.dp, top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stub.figure, style = AdventureType.display(60.sp), color = colors.primary, maxLines = 1)
                    Text(
                        text = stub.label,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                // The tear: a dashed line with a notch bitten out at each end.
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .drawBehind {
                            drawLine(
                                edge, Offset(size.width / 2, 18.dp.toPx()), Offset(size.width / 2, size.height - 18.dp.toPx()),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
                            )
                            val r = 12.dp.toPx()
                            drawCircle(background, r, Offset(size.width / 2, -1.dp.toPx()))
                            drawCircle(background, r, Offset(size.width / 2, size.height + 1.dp.toPx()))
                        }
                )
                Column(
                    Modifier.weight(1f).padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(dates ?: "No dates", style = AdventureType.display(24.sp))
                    if (checklist != null) {
                        val done = checklist.items.count { it.isChecked }
                        Text(
                            text = "${checklist.name} \u00b7 $done of ${checklist.items.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Expressive's wavy progress: the list filling up as the trip nears.
                        LinearWavyProgressIndicator(
                            progress = { done.toFloat() / checklist.items.size },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val places = collection.locations.size
                        Text(
                            text = if (places == 1) "1 place" else "$places places",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (collection.description.isNotBlank()) {
            Text(
                text = collection.description,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )
        }

        // Facts about the collection itself, in the same chips the rest of the app uses.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetaChip(
                text = if (collection.isPublic) "\uD83C\uDF0D Public" else "\uD83D\uDD12 Private",
                // Private is the default, not a problem: the error container shouted it in red at night.
                tone = ChipTone.NEUTRAL
            )
            // Archived only when it is: "✅ Active" was every trip's default, and beside a
            // COMPLETED status it read as a contradiction.
            if (collection.isArchived) {
                MetaChip(text = "\uD83D\uDCE6 Archived", tone = ChipTone.NEUTRAL)
            }
        }
    }
}

data class TransportationItem(
    val id: String,
    val name: String,
    val type: String,
    val imageUrl: String?,
    val isNotInItineraryDateRange: Boolean = false,
    /** "2 Nov 2026 · Madrid → Barcelona" - see [Transportation.summary]. */
    val summary: String? = null
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

                transportation.summary?.let { summary ->
                    Text(
                        text = summary,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

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
    onDelete: (() -> Unit)? = null,
    /** What the card is, for the confirmation: "note", "checklist", "stay". */
    kind: String = "item"
) {
    // One tap on the bin used to delete it outright (QA 04, CO-07). Places, collections and
    // transports all ask first; these now do too.
    var confirmDelete by remember { mutableStateOf(false) }
    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this $kind?") },
            text = { Text("\"$title\" will be deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }

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
                    IconButton(onClick = { confirmDelete = true }) {
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

/**
 * "1–3 Dec", "28 Nov – 3 Dec", or the one day that is set: the trip's own dates are written this
 * way on its ticket, and "01/12 – 03/12" read as a different app (and as January to some).
 */
private fun lodgingDates(checkIn: String?, checkOut: String?): String? =
    ticketDateRange(checkIn ?: checkOut, checkOut)
        ?.replace(Regex("\\b([A-Z])([A-Z]{2})\\b")) { it.groupValues[1] + it.groupValues[2].lowercase() }

// The rows of each kind, shared by its own tab and by All.

private fun LazyListScope.lodgingItems(
    lodging: List<Lodging>,
    onEdit: (Lodging) -> Unit,
    onDelete: (Lodging) -> Unit,
) {
    items(lodging, key = { "lodging-${it.id}" }) { stay ->
        SimpleEntryCard(
            onClick = { onEdit(stay) },
            onDelete = { onDelete(stay) },
            kind = "stay",
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

private fun LazyListScope.noteItems(
    notes: List<Note>,
    onEdit: (Note) -> Unit,
    onDelete: (Note) -> Unit,
) {
    items(notes, key = { "note-${it.id}" }) { note ->
        SimpleEntryCard(
            title = note.name,
            lines = listOfNotNull(
                note.content?.takeIf { it.isNotBlank() },
                note.date?.substringBefore('T')?.takeIf { it.isNotBlank() }
            ),
            badge = null,
            onClick = { onEdit(note) },
            onDelete = { onDelete(note) },
            kind = "note"
        )
    }
}

private fun LazyListScope.checklistItems(
    checklists: List<Checklist>,
    onEdit: (Checklist) -> Unit,
    onDelete: (Checklist) -> Unit,
) {
    items(checklists, key = { "checklist-${it.id}" }) { checklist ->
        val done = checklist.items.count { it.isChecked }
        SimpleEntryCard(
            onClick = { onEdit(checklist) },
            onDelete = { onDelete(checklist) },
            kind = "checklist",
            title = checklist.name,
            lines = checklist.items.take(4).map { item ->
                (if (item.isChecked) "✓ " else "○ ") + item.name
            } + listOfNotNull(
                "and ${checklist.items.size - 4} more"
                    .takeIf { checklist.items.size > 4 }
            ),
            badge = if (checklist.items.isEmpty()) null else "$done / ${checklist.items.size}"
        )
    }
}
