package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import com.desarrollodroide.adventurelog.core.model.Currencies
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import com.desarrollodroide.adventurelog.feature.ui.preview.PreviewImageDependencies
import com.desarrollodroide.adventurelog.core.model.userTags
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme

@Composable
fun AdventureItem(
    modifier: Modifier = Modifier,
    location: Location,
    collections: List<Collection> = emptyList(),
    onClick: () -> Unit = {},
    onOpenDetails: () -> Unit = { onClick() },
    onEdit: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    onShare: () -> Unit = {},
    onManageCollections: () -> Unit = {},
    onDelete: () -> Unit = {},
    showMenu: Boolean = true,
    /** False for someone else's place in a collection shared with you: see [LocationActionsSheet]. */
    isOwner: Boolean = true,
    onRemoveFromCollection: (() -> Unit)? = null
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imageLoader = LocalImageLoader.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            val hasImage = location.images.firstOrNull()?.image?.isNotEmpty() == true

            // A ratio, not a height. A fixed 250dp against a column that is 380dp wide on a
            // phone and 340dp on a tablet gave a tall card in one place and a square in the
            // other; a photograph asked to be square is a photograph with its ends cut off.
            val photo = Modifier.fillMaxWidth().aspectRatio(3f / 2f)
            if (hasImage) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = location.images.first().image,
                        imageLoader = imageLoader
                    ),
                    contentDescription = null,
                    modifier = photo,
                    contentScale = ContentScale.Crop
                )
            } else {
                LocationPlaceholder(
                    name = location.name,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    modifier = photo
                )
            }

            // Over the photograph, on a scrim.
            //
            // This sat under the photograph for a while, because white text on a bright sky
            // disappears. The scrim is the answer to that rather than a second block of card:
            // it darkens only the band the text occupies, and it reaches 0.92 at the bottom
            // edge, which holds even over snow. Whatever the photograph is doing, the strip
            // under the writing is dark.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                location.location?.takeIf { it.isNotBlank() }?.let { label ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val rating = location.rating?.toInt() ?: 0
                val category = location.category
                if (rating > 0 || category != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (rating > 0) {
                            Text(
                                text = "\u2605".repeat(rating.coerceAtMost(5)),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        if (category != null) {
                            Text(
                                text = category.displayName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                location.price?.let { price ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\uD83D\uDCB0 ${Currencies.formatAmount(price)} " +
                            (location.priceCurrency ?: Currencies.DEFAULT),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                val tags = location.tags.userTags()
                val collectionNames = location.collections.mapNotNull { id ->
                    collections.find { it.id == id }?.name
                }
                if (!location.isPublic || collectionNames.isNotEmpty() || tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!location.isPublic) {
                            MetaChip(text = "\uD83D\uDD12 Private", tone = ChipTone.WARNING)
                        }
                        val visibleCollections = collectionNames.take(2)
                        visibleCollections.forEach { name ->
                            MetaChip(text = "\uD83D\uDCC1 $name", tone = ChipTone.ON_IMAGE)
                        }
                        val remainingCollections = collectionNames.size - visibleCollections.size
                        if (remainingCollections > 0) {
                            MetaChip(text = "+$remainingCollections", tone = ChipTone.ON_IMAGE)
                        }
                        val visibleTags = tags.take(3)
                        visibleTags.forEach { tag ->
                            MetaChip(text = tag, tone = ChipTone.ON_IMAGE)
                        }
                        val hiddenTags = tags.size - visibleTags.size
                        if (hiddenTags > 0) {
                            MetaChip(text = "+$hiddenTags", tone = ChipTone.ON_IMAGE)
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

                }
            }
        }
    }
    
    if (showDropdownMenu) {
        LocationActionsSheet(
            locationName = location.name,
            locationPlace = location.location,
            onDismiss = { showDropdownMenu = false },
            onOpenDetails = { showDropdownMenu = false; onOpenDetails() },
            onEdit = { showDropdownMenu = false; onEdit() },
            onDuplicate = { showDropdownMenu = false; onDuplicate() },
            onShare = { showDropdownMenu = false; onShare() },
            onManageCollections = { showDropdownMenu = false; onManageCollections() },
            onDelete = { showDropdownMenu = false; showDeleteDialog = true },
            isOwner = isOwner,
            onRemoveFromCollection = onRemoveFromCollection?.let { remove -> { showDropdownMenu = false; remove() } }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete place") },
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
