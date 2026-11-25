package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCollectionsDialog(
    location: Location,
    allCollections: List<UltraSlimCollection>,
    isLoadingCollections: Boolean = false,
    onUpdateCollections: (adventureId: String, collectionIds: List<String>) -> Unit,
    onRefreshCollections: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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

/**
 * The place's collections first, then the rest, each set one segmented block of check rows; the
 * search narrows only the rest, so what is already chosen never disappears while typing.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ManageCollectionsContent(
    location: Location,
    allCollections: List<UltraSlimCollection>,
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
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = "Manage collections", style = AdventureType.display(34.sp))
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            onRefreshCollections?.let { refresh ->
                IconButton(
                    onClick = refresh,
                    enabled = !isLoadingCollections,
                    shapes = IconButtonDefaults.shapes()
                ) {
                    if (isLoadingCollections) {
                        LoadingIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Refresh collections",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            IconButton(onClick = onDismiss, shapes = IconButtonDefaults.shapes()) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        StandardSearchField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search collections..."
        )

        // How many are chosen, with a way to clear them, while any are.
        AnimatedVisibility(
            visible = selectedCollections.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedCollections.size} collection${if (selectedCollections.size != 1) "s" else ""} selected",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { selectedCollections = emptySet() },
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Clear all")
                }
            }
        }

        if (isLoadingCollections && allCollections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoadingIndicator()
                    Text(
                        text = "Loading collections...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (sortedAndFilteredCollections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CookieBadge(
                        icon = if (searchQuery.isNotEmpty()) Icons.Outlined.SearchOff else Icons.Outlined.Inventory2,
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        content = MaterialTheme.colorScheme.onSecondaryContainer,
                        size = 72.dp
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No collections found" else "No collections available",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search" else "Create your first collection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                val selectedItems = sortedAndFilteredCollections.filter {
                    selectedCollections.contains(it.id)
                }
                val unselectedItems = sortedAndFilteredCollections.filter {
                    !selectedCollections.contains(it.id)
                }

                if (selectedItems.isNotEmpty()) {
                    item(key = "header-selected") {
                        SectionLabel("Selected", MaterialTheme.colorScheme.primary, Modifier.animateItem())
                    }
                    itemsIndexed(items = selectedItems, key = { _, it -> it.id }) { index, collection ->
                        CollectionSelectionItem(
                            collection = collection,
                            isSelected = true,
                            index = index,
                            count = selectedItems.size,
                            onSelectionChange = {
                                selectedCollections = selectedCollections - collection.id
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                if (unselectedItems.isNotEmpty()) {
                    if (selectedItems.isNotEmpty()) {
                        item(key = "header-available") {
                            SectionLabel(
                                "Available",
                                MaterialTheme.colorScheme.onSurfaceVariant,
                                Modifier.animateItem().padding(top = 12.dp)
                            )
                        }
                    }
                    itemsIndexed(items = unselectedItems, key = { _, it -> it.id }) { index, collection ->
                        CollectionSelectionItem(
                            collection = collection,
                            isSelected = false,
                            index = index,
                            count = unselectedItems.size,
                            onSelectionChange = {
                                selectedCollections = selectedCollections + collection.id
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        onUpdateCollections(location.id, selectedCollections.toList())
                        onDismiss()
                    }
                },
                enabled = !isLoading && hasChanges,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                if (isLoading) {
                    LoadingIndicator(modifier = Modifier.size(24.dp), color = LocalContentColor.current)
                } else {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Save", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        modifier = modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

/** A collection as a check row: a filled circle with a tick when the place is in it, a ring when not. */
@Composable
private fun CollectionSelectionItem(
    collection: UltraSlimCollection,
    isSelected: Boolean,
    index: Int,
    count: Int,
    onSelectionChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Color>()
    val container by animateColorAsState(
        if (isSelected) colors.secondaryContainer else colors.surfaceContainerHigh, effects, label = "row"
    )
    Surface(
        shape = SegmentedListDefaults.shapeFor(index, count),
        color = container,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .toggleable(value = isSelected, onValueChange = { onSelectionChange() }, role = Role.Checkbox)
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CheckCircle(checked = isSelected)

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
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) colors.onSecondaryContainer else colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (collection.isPublic) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surfaceContainerHighest,
                            contentColor = colors.onSurfaceVariant
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(12.dp))
                                Text("Public", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                val details = buildList {
                    if (collection.adventureCount > 0) {
                        add("${collection.adventureCount} place${if (collection.adventureCount != 1) "s" else ""}")
                    }
                    if (collection.description.isNotEmpty()) add(collection.description)
                }
                if (details.isNotEmpty()) {
                    Text(
                        text = details.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) colors.onSecondaryContainer else colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** The multi-choice mark: filled primary with a tick when on, an outline ring when off. */
@Composable
private fun CheckCircle(checked: Boolean) {
    val colors = MaterialTheme.colorScheme
    val fill by animateColorAsState(
        if (checked) colors.primary else Color.Transparent,
        MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "check"
    )
    Box(
        Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(fill)
            .then(if (checked) Modifier else Modifier.border(2.dp, colors.outline, CircleShape)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = scaleIn(MaterialTheme.motionScheme.fastSpatialSpec(), initialScale = 0.3f) + fadeIn(),
            exit = scaleOut(targetScale = 0.3f) + fadeOut()
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(18.dp))
        }
    }
}
