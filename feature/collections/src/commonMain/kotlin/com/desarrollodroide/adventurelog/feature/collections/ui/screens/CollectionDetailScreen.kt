package com.desarrollodroide.adventurelog.feature.collections.ui.screens

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
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionsTabs
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionDetailViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.DeleteState
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.UpdateCollectionsState
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog
import com.desarrollodroide.adventurelog.feature.ui.components.ManageCollectionsDialog
import com.desarrollodroide.adventurelog.feature.ui.components.TagChip
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    onAddTransportation: () -> Unit,
    onEditTransportation: (Transportation) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
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
                snackbarHostState.showSnackbar("Adventure deleted successfully")
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
                    selectedTab = selectedTab,
                    onTabSelected = viewModel::onTabSelected,
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
    selectedTab: CollectionTab,
    onTabSelected: (CollectionTab) -> Unit,
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    onDeleteAdventure: (Location) -> Unit,
    onManageCollections: (Location) -> Unit,
    onAddTransportation: () -> Unit,
    onEditTransportation: (Transportation) -> Unit,
    onDeleteTransportation: (Transportation) -> Unit,
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
            CollectionHeader(collection)
        }
        
        item {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                CollectionsTabs(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )
            }
        }
        
        when (selectedTab) {
            CollectionTab.ALL -> {
                // Show Locations section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Locations",
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
                            text = "Locations",
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Description (only if not blank)
        if (collection.description.isNotBlank()) {
            Text(
                text = collection.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Stats row - only visibility and status (adventures count is shown in the section header)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visibility chip
            AssistChip(
                onClick = { },
                label = { 
                    Text(
                        text = if (collection.isPublic) "Public" else "Private"
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (collection.isPublic) Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.padding(end = 8.dp)
            )
            
            // Status chip
            AssistChip(
                onClick = { },
                label = { 
                    Text(
                        text = if (collection.isArchived) "Archived" else "Active"
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (collection.isArchived) Icons.Default.Archive else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
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
                    TagChip(
                        text = getTransportationIcon(transportation.type) + " " + transportation.type.replaceFirstChar { it.uppercase() },
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )

                    // Not in itinerary date range tag
                    if (transportation.isNotInItineraryDateRange) {
                        TagChip(
                            text = "Not in itinerary date range",
                            backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onError
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
