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
import com.desarrollodroide.adventurelog.core.model.AdventureImage
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.feature.detail.ui.components.MapView
import com.desarrollodroide.adventurelog.feature.detail.viewmodel.AdventureDetailViewModel
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState

@Composable
fun AdventureDetailScreenRoute(
    adventure: Adventure,
    onBackClick: () -> Unit
) {
    val viewModel = koinViewModel<AdventureDetailViewModel>()

    AdventureDetailScreen(
        adventure = adventure,
        onBackClick = onBackClick,
        onEditClick = { viewModel.editAdventure(adventure.id) },
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
            CoverImageWithButtons(
                imageUrl = adventure.images.firstOrNull()?.image,
                onBackClick = onBackClick,
                onShareClick = { /* TODO: Implement share */ }
            )
            
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
                    HeaderInfo(
                        title = adventure.name,
                        location = adventure.location
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryTags(
                        category = adventure.category,
                        isPublic = adventure.isPublic,
                        collection = adventure.collection
                    )

                    if (adventure.images.isNotEmpty()) {
                        PhotosCarousel(images = adventure.images)
                    }
                    
                    AboutSection(description = adventure.description)
                    
                    if (adventure.latitude.isNotEmpty() && adventure.longitude.isNotEmpty()) {
                        MapSection(
                            latitude = adventure.latitude,
                            longitude = adventure.longitude,
                            location = adventure.location,
                            onOpenMap = onOpenMap
                        )
                    }
                    
                    adventure.link?.let { link ->
                        if (link.isNotEmpty()) {
                            LinkSection(link = link, onOpenLink = onOpenLink)
                        }
                    }
                    
                    if (adventure.visits.isNotEmpty()) {
                        VisitsSection(visits = adventure.visits)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    CreationInfo(
                        createdAt = adventure.createdAt,
                        updatedAt = adventure.updatedAt
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverImageWithButtons(
    imageUrl: String?,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageLoader = LocalImageLoader.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        imageUrl?.let {
            Image(
                painter = rememberAsyncImagePainter(
                    model = it,
                    imageLoader = imageLoader
                ),
                contentDescription = "Adventure image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(start = 16.dp, top = 40.dp)
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

        IconButton(
            onClick = onShareClick,
            modifier = Modifier
                .padding(end = 16.dp, top = 40.dp)
                .size(40.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f))
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun HeaderInfo(
    title: String,
    location: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (!location.isNullOrEmpty()) {
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryTags(
    category: Category?,
    isPublic: Boolean,
    collection: String?,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        category?.let {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = "${it.icon} ${it.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        if (!isPublic) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotosCarousel(
    images: List<AdventureImage>,
    modifier: Modifier = Modifier
) {
    val imageLoader = LocalImageLoader.current
    
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Photos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val carouselState = rememberCarouselState { images.size }

        HorizontalMultiBrowseCarousel(
            state = carouselState,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            preferredItemWidth = 120.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) { index ->
            val image = images[index]
            
            Box(
                modifier = Modifier.maskClip(shape = RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = image.image,
                        imageLoader = imageLoader
                    ),
                    contentDescription = "Adventure image ${index + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AboutSection(
    description: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "About",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        if (!description.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MapSection(
    latitude: String,
    longitude: String,
    location: String?,
    onOpenMap: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
                latitude = latitude,
                longitude = longitude,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LinkSection(
    link: String,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
}

@Composable
private fun VisitsSection(
    visits: List<Visit>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Visits",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        visits.forEach { visit ->
            VisitItem(visit = visit)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun VisitItem(
    visit: Visit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
}

@Composable
private fun CreationInfo(
    createdAt: String,
    updatedAt: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Created: ${createdAt.take(10)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Updated: ${updatedAt.take(10)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Previews
/**
 * Creates an adventure with multiple images for testing the carousel
 */
private fun createAdventureWithMultipleImages(): Adventure {
    // Create a list of images for the carousel
    val images = listOf(
        AdventureImage(
            id = "img1",
            image = "https://images.unsplash.com/photo-1571896349842-33c89424de2d",
            adventure = "adv1",
            isPrimary = true,
            userId = "user123"
        ),
        AdventureImage(
            id = "img2",
            image = "https://images.unsplash.com/photo-1566073771259-6a8506099945",
            adventure = "adv1",
            isPrimary = false,
            userId = "user123"
        ),
        AdventureImage(
            id = "img3",
            image = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
            adventure = "adv1",
            isPrimary = false,
            userId = "user123"
        ),
        AdventureImage(
            id = "img4",
            image = "https://images.unsplash.com/photo-1554995207-c18c203602cb",
            adventure = "adv1",
            isPrimary = false,
            userId = "user123"
        ),
        AdventureImage(
            id = "img5",
            image = "https://images.unsplash.com/photo-1560624052-449f5ddf0c31",
            adventure = "adv1",
            isPrimary = false,
            userId = "user123"
        )
    )
    
    return Adventure(
        id = "adv1",
        userId = "user123",
        name = "Mountain Adventure",
        description = "An amazing mountain adventure with breathtaking views and challenging trails. Perfect for hiking enthusiasts looking for their next great outdoor experience.",
        rating = 4.5,
        activityTypes = listOf("Hiking", "Nature", "Photography"),
        location = "Rocky Mountains, Colorado",
        isPublic = true,
        collection = "Summer Adventures",
        createdAt = "2025-01-15T10:00:00.000Z",
        updatedAt = "2025-01-20T14:30:00.000Z",
        images = images,
        link = "https://example.com/mountain-adventure",
        longitude = "-105.643240",
        latitude = "39.739236",
        visits = listOf(
            Visit(
                id = "visit1",
                startDate = "2025-01-15",
                endDate = "2025-01-17",
                notes = "Great weather, saw amazing wildlife!"
            )
        ),
        isVisited = true,
        category = Category(
            id = "cat1",
            name = "hiking",
            displayName = "Hiking",
            icon = "🥾",
            numAdventures = 10
        ),
        attachments = emptyList()
    )
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdventureDetailScreenLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                adventure = createAdventureWithMultipleImages(),
                onBackClick = {},
                onEditClick = {},
                onOpenMap = { _, _ -> },
                onOpenLink = {}
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdventureDetailScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                adventure = createAdventureWithMultipleImages().copy(
                    name = "Night Sky Photography Tour",
                    description = "Experience the wonders of astrophotography in one of the darkest skies in North America.",
                    activityTypes = listOf("Photography", "Astronomy", "Night Tour"),
                    isPublic = false
                ),
                onBackClick = {},
                onEditClick = {},
                onOpenMap = { _, _ -> },
                onOpenLink = {}
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun HotelBalnearioDetailPreview() {
    val hotelBalneario = Adventure(
        id = "c9cfb44c-536a-492c-87ff-8c3bb5d3eec5",
        userId = "user123",
        name = "Gran Hotel Balneario De Puente Viesgo (Pendiente) - 4 h 28 min (445 km)",
        description = "Spa termal en Cantabria con aguas termales y tratamientos de bienestar",
        rating = 3.4,
        activityTypes = listOf("Spa", "Relax", "Turismo"),
        location = "4h 28min (445 km)",
        isPublic = false,
        collection = "Santander",
        createdAt = "2025-03-01T10:00:00.000Z",
        updatedAt = "2025-03-15T14:30:00.000Z",
        images = listOf(
            AdventureImage(
                id = "img1",
                image = "https://images.unsplash.com/photo-1571896349842-33c89424de2d",
                adventure = "c9cfb44c-536a-492c-87ff-8c3bb5d3eec5",
                isPrimary = true,
                userId = "user123"
            ),
            AdventureImage(
                id = "img2",
                image = "https://images.unsplash.com/photo-1584132915807-fd1f5fbc078f",
                adventure = "c9cfb44c-536a-492c-87ff-8c3bb5d3eec5",
                isPrimary = false,
                userId = "user123"
            )
        ),
        link = "https://www.booking.com/hotel/es/gran-balneario-de-puente-viesgo.es.html",
        longitude = "-3.965588",
        latitude = "43.299242",
        visits = emptyList(),
        isVisited = false,
        category = Category(
            id = "cat1",
            name = "hotel",
            displayName = "Hotel",
            icon = "🏨",
            numAdventures = 5
        ),
        attachments = emptyList()
    )

    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                adventure = hotelBalneario,
                onBackClick = {},
                onEditClick = {},
                onOpenMap = { _, _ -> },
                onOpenLink = {}
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun NavalagamellaDetailPreview() {
    val navalagamella = Adventure(
        id = "2ac911dd-8742-45e6-b105-5c04779e8bea",
        userId = "user123",
        name = "Navalagamella",
        description = "Ruta de los molinos con paisajes naturales y arroyos",
        rating = 2.2,
        activityTypes = listOf("Senderismo", "Naturaleza"),
        location = "",
        isPublic = false,
        collection = "Madrid",
        createdAt = "2025-02-20T09:15:00.000Z",
        updatedAt = "2025-03-10T11:45:00.000Z",
        images = listOf(
            AdventureImage(
                id = "img2",
                image = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
                adventure = "2ac911dd-8742-45e6-b105-5c04779e8bea",
                isPrimary = true,
                userId = "user123"
            ),
            AdventureImage(
                id = "img3",
                image = "https://images.unsplash.com/photo-1551632811-561732d1e306",
                adventure = "2ac911dd-8742-45e6-b105-5c04779e8bea",
                isPrimary = false,
                userId = "user123"
            )
        ),
        link = "https://sendasdeviaje.com/navalagamella-ruta-molinos/",
        longitude = "-4.122708",
        latitude = "40.469059",
        visits = emptyList(),
        isVisited = false,
        category = Category(
            id = "cat2",
            name = "ruta",
            displayName = "Ruta",
            icon = "🏞️",
            numAdventures = 3
        ),
        attachments = emptyList()
    )

    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                adventure = navalagamella,
                onBackClick = {},
                onEditClick = {},
                onOpenMap = { _, _ -> },
                onOpenLink = {}
            )
        }
    }
}