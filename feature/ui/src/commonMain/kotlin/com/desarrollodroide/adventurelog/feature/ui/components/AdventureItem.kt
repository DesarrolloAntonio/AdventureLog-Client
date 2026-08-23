package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import com.desarrollodroide.adventurelog.feature.ui.preview.PreviewImageDependencies

@Composable
fun AdventureItem(
    modifier: Modifier = Modifier,
    location: Location,
    collections: List<Collection> = emptyList(),
    onClick: () -> Unit = {},
    onOpenDetails: () -> Unit = { onClick() },
    onEdit: () -> Unit = {},
    onManageCollections: () -> Unit = {},
    onDelete: () -> Unit = {},
    showMenu: Boolean = true
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imageLoader = LocalImageLoader.current

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box {
            val hasImage = location.images.firstOrNull()?.image?.isNotEmpty() == true
            
            if (hasImage) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = location.images.first().image,
                        imageLoader = imageLoader
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                LocationPlaceholder(
                    name = location.name,
                    latitude = location.latitude,
                    longitude = location.longitude,

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
                    text = location.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (location.category != null || !location.isPublic || location.collections.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Category tag
                        location.category?.let { category ->
                            TagChip(
                                text = "${category.icon} ${category.displayName}",
                                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // Private tag
                        if (!location.isPublic) {
                            TagChip(
                                text = "🔒 Private",
                                backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        }

                        // Collection tags
                        val collectionNames = location.collections.mapNotNull { id ->
                            collections.find { it.id == id }?.name
                        }

                        // Show first 2 collections and a +N indicator if there are more
                        val visibleCollections = collectionNames.take(2)
                        val remainingCount = collectionNames.size - visibleCollections.size

                        visibleCollections.forEach { collectionName ->
                            TagChip(
                                text = "📁 $collectionName",
                                backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        }

                        if (remainingCount > 0) {
                            TagChip(
                                text = "+$remainingCount",
                                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (showMenu) {
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
                            text = { Text("Open Details") },
                            onClick = {
                                onOpenDetails()
                                showDropdownMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Location") },
                            onClick = {
                                onEdit()
                                showDropdownMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Manage Collections") },
                            onClick = {
                                onManageCollections()
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
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Location") },
            text = { Text("Are you sure you want to delete \"${location.name}\"? This action cannot be undone.") },
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

// Previews
@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdventureItemLightPreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background) {
                Box(modifier = Modifier.padding(16.dp)) {
                    AdventureItem(
                        location = PreviewData.locations[0],
                        collections = PreviewData.collections,
                        onOpenDetails = {},
                        onEdit = {},
                        onManageCollections = {},
                        onDelete = {}
                    )
                }
            }
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdventureItemDarkPreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background) {
                Box(modifier = Modifier.padding(16.dp)) {
                    AdventureItem(
                        location = PreviewData.locations[1],
                        collections = PreviewData.collections,
                        onOpenDetails = {},
                        onEdit = {},
                        onManageCollections = {},
                        onDelete = {}
                    )
                }
            }
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdventureItemPrivatePreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background) {
                Box(modifier = Modifier.padding(16.dp)) {
                    AdventureItem(
                        location = PreviewData.locations[0],
                        collections = PreviewData.collections,
                        onOpenDetails = {},
                        onEdit = {},
                        onManageCollections = {},
                        onDelete = {}
                    )
                }
            }
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdventureItemNoImagePreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background) {
                Box(modifier = Modifier.padding(16.dp)) {
                    AdventureItem(
                        location = PreviewData.locations[0].copy(
                            images = emptyList(), // No images
                            category = Category(
                                id = "cat1",
                                name = "hiking",
                                displayName = "Hiking",
                                icon = "🥾",
                                numAdventures = "10"
                            )
                        ),
                        collections = PreviewData.collections,
                        onOpenDetails = {},
                        onEdit = {},
                        onManageCollections = {},
                        onDelete = {}
                    )
                }
            }
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdventureItemNoImageMountainPreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdventureItem(
                        location = PreviewData.locations[0].copy(
                            images = emptyList(),
                            name = "Epic Mountain Trail",
                            category = Category(
                                id = "cat1",
                                name = "mountain",
                                displayName = "Mountain",
                                icon = "⛰️",
                                numAdventures = "10"
                            )
                        ),
                        collections = PreviewData.collections
                    )
                    
                    AdventureItem(
                        location = PreviewData.locations[0].copy(
                            images = emptyList(),
                            name = "Beach Paradise Getaway",
                            category = Category(
                                id = "cat2",
                                name = "beach",
                                displayName = "Beach",
                                icon = "🏖️",
                                numAdventures = "10"
                            ),
                            isPublic = false,
                            collections = listOf("1", "2", "3") // Multiple collections
                        ),
                        collections = PreviewData.collections
                    )
                }
            }
        }
    }
}
