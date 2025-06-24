package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import com.desarrollodroide.adventurelog.feature.ui.di.LocalSessionTokenManager
import com.desarrollodroide.adventurelog.feature.ui.preview.PreviewImageDependencies

@Composable
fun AdventureItem(
    modifier: Modifier = Modifier,
    adventure: Adventure,
    collections: List<Collection> = emptyList(),
    onClick: () -> Unit = {},
    onOpenDetails: () -> Unit = { onClick() },
    onEdit: () -> Unit = {},
    onRemoveFromCollection: () -> Unit = {},
    onDelete: () -> Unit = {},
    sessionToken: String = ""
) {
    var showMenu by remember { mutableStateOf(false) }

    val imageLoader = LocalImageLoader.current
    val sessionTokenManager = LocalSessionTokenManager.current

    LaunchedEffect(sessionToken) {
        if (sessionToken.isNotEmpty()) {
            sessionTokenManager.updateSessionToken(sessionToken)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(
                    model = adventure.images.firstOrNull()?.image ?: "",
                    imageLoader = imageLoader
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

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
                    text = adventure.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (adventure.category != null || !adventure.isPublic || adventure.collections.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Category tag
                        adventure.category?.let { category ->
                            TagChip(
                                text = "${category.icon} ${category.displayName}",
                                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // Private tag
                        if (!adventure.isPublic) {
                            TagChip(
                                text = "🔒 Private",
                                backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        }

                        // Collection tags
                        val collectionNames = adventure.collections.mapNotNull { id ->
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
                        onClick = { showMenu = true },
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
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open Details") },
                        onClick = {
                            onOpenDetails()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit Adventure") },
                        onClick = {
                            onEdit()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove from collection") },
                        onClick = {
                            onRemoveFromCollection()
                            showMenu = false
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete",
                                color = Color(0xFFFF3B30)
                            )
                        },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

// Previews
@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdventureItemLightPreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background) {
                AdventureItem(
                    adventure = PreviewData.adventures[0],
                    collections = PreviewData.collections,
                    onOpenDetails = {},
                    onEdit = {},
                    onRemoveFromCollection = {},
                    onDelete = {}
                )
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
                AdventureItem(
                    adventure = PreviewData.adventures[1],
                    collections = PreviewData.collections,
                    onOpenDetails = {},
                    onEdit = {},
                    onRemoveFromCollection = {},
                    onDelete = {}
                )
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
                AdventureItem(
                    adventure = PreviewData.adventures[0],
                    collections = PreviewData.collections,
                    onOpenDetails = {},
                    onEdit = {},
                    onRemoveFromCollection = {},
                    onDelete = {}
                )
            }
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdventureItemWithCollectionPreview() {
    PreviewImageDependencies {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Surface(color = MaterialTheme.colorScheme.background) {
                AdventureItem(
                    adventure = PreviewData.adventures[1],
                    collections = PreviewData.collections,
                    onOpenDetails = {},
                    onEdit = {},
                    onRemoveFromCollection = {},
                    onDelete = {}
                )
            }
        }
    }
}