package com.desarrollodroide.adventurelog.feature.detail.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.feature.detail.ui.components.MapView
import com.desarrollodroide.adventurelog.feature.detail.viewmodel.AdventureDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import com.desarrollodroide.adventurelog.feature.ui.utils.maskClip

/**
 * Entry point composable that integrates with navigation
 */
@Composable
fun AdventureDetailScreenRoute(
    adventureId: String,
    onBackClick: () -> Unit
) {
    val viewModel = koinViewModel<AdventureDetailViewModel>()
    val adventure = viewModel.getAdventureById(adventureId)

    AdventureDetailScreen(
        adventure = adventure,
        onBackClick = onBackClick,
        onEditClick = { viewModel.editAdventure(adventureId) },
        onOpenMap = { lat, long -> viewModel.openMap(lat, long) },
        onOpenLink = { url -> viewModel.openLink(url) }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdventureDetailScreen(
    adventure: Adventure,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onOpenMap: (String, String) -> Unit,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Cover image with overlay buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Main image (just the first image)
                if (adventure.images.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = adventure.images.first().image
                        ),
                        contentDescription = "Adventure image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Back button overlay
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Favorite button
                    IconButton(
                        onClick = { /* TODO: Implement favorite */ },
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = Color.Black
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = { /* TODO: Implement share */ },
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.Black
                        )
                    }
                }
            }

            // Content card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Title
                    Text(
                        text = adventure.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Location
                    adventure.location?.let { location ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Tags section - FlowRow with Tags
                    Spacer(modifier = Modifier.height(16.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category
                        adventure.category?.let { category ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Text(
                                    text = "${category.icon} ${category.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Private indicator
                        if (!adventure.isPublic) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Text(
                                    text = "Private",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Collection tag if present
                        adventure.collection?.let { collection ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Text(
                                    text = "Collection: $collection",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (adventure.images.size > 1) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Photos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Create a state for the carousel
                        val carouselState = rememberCarouselState { adventure.images.size }

                        HorizontalMultiBrowseCarousel(
                            state = carouselState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            preferredItemWidth = 120.dp,
                            itemSpacing = 8.dp,
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) { index ->
                            val image = adventure.images[index]
                            
                            Box(
                                modifier = Modifier.maskClip(shape = RoundedCornerShape(16.dp))
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = image.image),
                                    contentDescription = "Adventure image ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    
                    // About section
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Description
                    adventure.description?.let { description ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Map section
                    if (adventure.latitude != null && adventure.longitude != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            MapView(
                                latitude = adventure.latitude,
                                longitude = adventure.longitude,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            onClick = { 
                                adventure.latitude?.let { lat ->
                                    adventure.longitude?.let { long ->
                                        onOpenMap(lat, long)
                                    }
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "View on maps",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = adventure.location ?: "Open in maps app",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Link
                    adventure.link?.let { link ->
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Link",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            onClick = { onOpenLink(link) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Link",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = link,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    // Visits
                    if (adventure.visits.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Visits",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        adventure.visits.forEach { visit ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "From: ${visit.startDate}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        Text(
                                            text = "To: ${visit.endDate}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    
                                    visit.notes?.let { notes ->
                                        if (notes.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = notes,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    // Creation and update information
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Created: ${adventure.createdAt.take(10)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Updated: ${adventure.updatedAt.take(10)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
