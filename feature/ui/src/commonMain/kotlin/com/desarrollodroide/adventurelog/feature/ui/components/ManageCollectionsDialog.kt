package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Collection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCollectionsDialog(
    location: Location,
    allCollections: List<Collection>,
    isLoadingCollections: Boolean = false,
    onUpdateCollections: (adventureId: String, collectionIds: List<String>) -> Unit,
    onRefreshCollections: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 4.dp)
                )
            }
        }
    ) {
        ManageCollectionsContent(
            location = location,
            allCollections = allCollections,
            isLoadingCollections = isLoadingCollections,
            onUpdateCollections = onUpdateCollections,
            onRefreshCollections = onRefreshCollections,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageCollectionsContent(
    location: Location,
    allCollections: List<Collection>,
    isLoadingCollections: Boolean,
    onUpdateCollections: (adventureId: String, collectionIds: List<String>) -> Unit,
    onRefreshCollections: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    var selectedCollections by remember { 
        mutableStateOf(location.collections.toSet())
    }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    // Sort collections - selected ones first, then alphabetically
    val sortedAndFilteredCollections = remember(searchQuery, allCollections, selectedCollections) {
        // Separate selected and unselected
        val selected = allCollections.filter { selectedCollections.contains(it.id) }
        val unselected = allCollections.filter { !selectedCollections.contains(it.id) }
        
        // Apply search filter ONLY to unselected collections
        val filteredUnselected = if (searchQuery.isBlank()) {
            unselected
        } else {
            unselected.filter { 
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        // Combine: selected first (unfiltered), then filtered unselected
        selected.sortedBy { it.name.lowercase() } + 
        filteredUnselected.sortedBy { it.name.lowercase() }
    }
    
    val hasChanges = remember(selectedCollections, location.collections) {
        selectedCollections != location.collections.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Manage Collections",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // Actions row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Refresh button
                onRefreshCollections?.let { refresh ->
                    IconButton(
                        onClick = refresh,
                        enabled = !isLoadingCollections
                    ) {
                        if (isLoadingCollections) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Refresh collections",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Close button
                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Search bar
        StandardSearchField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search collections...",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Selected count
        AnimatedVisibility(
            visible = selectedCollections.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${selectedCollections.size} collection${if (selectedCollections.size != 1) "s" else ""} selected",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    TextButton(
                        onClick = { selectedCollections = emptySet() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            "Clear all",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
        
        // Collections list
        if (isLoadingCollections && allCollections.isEmpty()) {
            // Loading state when no collections are available
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading collections...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (sortedAndFilteredCollections.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (searchQuery.isNotEmpty()) 
                            Icons.Outlined.SearchOff else Icons.Outlined.Inventory2,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .alpha(0.4f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) 
                            "No collections found" else "No collections available",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) 
                            "Try a different search" else "Create your first collection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Add section headers
                val selectedItems = sortedAndFilteredCollections.filter { 
                    selectedCollections.contains(it.id) 
                }
                val unselectedItems = sortedAndFilteredCollections.filter { 
                    !selectedCollections.contains(it.id) 
                }
                
                if (selectedItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "SELECTED",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                    
                    items(
                        items = selectedItems,
                        key = { it.id }
                    ) { collection ->
                        CollectionSelectionItem(
                            collection = collection,
                            isSelected = true,
                            onSelectionChange = {
                                selectedCollections = selectedCollections - collection.id
                            }
                        )
                    }
                }
                
                if (unselectedItems.isNotEmpty()) {
                    if (selectedItems.isNotEmpty()) {
                        item {
                            Text(
                                text = "AVAILABLE",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(
                                    top = 16.dp,
                                    bottom = 8.dp,
                                    start = 4.dp,
                                    end = 4.dp
                                )
                            )
                        }
                    }
                    
                    items(
                        items = unselectedItems,
                        key = { it.id }
                    ) { collection ->
                        CollectionSelectionItem(
                            collection = collection,
                            isSelected = false,
                            onSelectionChange = {
                                selectedCollections = selectedCollections + collection.id
                            }
                        )
                    }
                }
            }
        }
        
        // Bottom actions with divider
        Column {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        "Cancel",
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            onUpdateCollections(location.id, selectedCollections.toList())
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && hasChanges,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save",
                            modifier = Modifier.padding(vertical = 6.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionSelectionItem(
    collection: Collection,
    isSelected: Boolean,
    onSelectionChange: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = if (isSelected) {
            BorderStroke(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        } else null,
        onClick = onSelectionChange
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )
            
            // Collection info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Badges with more subtle colors
                    if (collection.isPublic) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Public,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Public",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (collection.locations.isNotEmpty()) {
                        Text(
                            "${collection.locations.size} adventure${if (collection.locations.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Description
                collection.description?.let { description ->
                    if (description.isNotEmpty()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
