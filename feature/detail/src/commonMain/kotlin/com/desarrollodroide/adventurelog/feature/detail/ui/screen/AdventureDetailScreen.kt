package com.desarrollodroide.adventurelog.feature.detail.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import com.desarrollodroide.adventurelog.feature.detail.ui.components.AttachmentsSection
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.ContentImage
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.City
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.detail.ui.components.*
import com.desarrollodroide.adventurelog.feature.detail.viewmodel.AdventureDetailViewModel
import com.desarrollodroide.adventurelog.feature.detail.viewmodel.LocationState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AdventureDetailScreenRoute(
    locationId: String,
    onBackClick: () -> Unit
) {
    val viewModel = koinViewModel<AdventureDetailViewModel>()
    
    LaunchedEffect(locationId) {
        viewModel.loadLocation(locationId)
    }
    
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val openingAttachmentId by viewModel.openingAttachmentId.collectAsStateWithLifecycle()
    val attachmentMessage by viewModel.attachmentMessage.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(attachmentMessage) {
        attachmentMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearAttachmentMessage()
        }
    }

    when (val state = locationState) {
        is LocationState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is LocationState.Success -> {
            Box(modifier = Modifier.fillMaxSize()) {
                AdventureDetailScreen(
                    location = state.location,
                    collections = collections,
                    onBackClick = onBackClick,
                    onEditClick = { viewModel.editAdventure(state.location.id) },
                    onOpenMap = { lat: String, long: String -> viewModel.openMap(lat, long) },
                    // The link is a plain external URL, so the platform handler is enough - it
                    // used to be routed to a view model method that only printed it.
                    onOpenLink = { url: String -> uriHandler.openUri(url) },
                    openingAttachmentId = openingAttachmentId,
                    onOpenAttachment = viewModel::openAttachment
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
        is LocationState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        text = "Error loading location",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}

@Composable
fun AdventureDetailScreen(
    location: Location,
    collections: List<UltraSlimCollection> = emptyList(),
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onOpenMap: (String, String) -> Unit,
    onOpenLink: (String) -> Unit,
    openingAttachmentId: String? = null,
    onOpenAttachment: (com.desarrollodroide.adventurelog.core.model.Attachment) -> Unit = {},
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
                imageUrl = location.images.firstOrNull()?.image,
                adventureName = location.name,
                onBackClick = onBackClick,
                onShareClick = { /* TODO: Implement share */ }
            )

            // Content container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    HeaderInfo(
                        title = location.name,
                        location = location.location,
                        rating = location.rating
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryTags(
                        category = location.category,
                        isPublic = location.isPublic,
                        tags = location.tags
                    )

                    // Collections section
                    if (collections.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CollectionsSection(
                            collections = collections,
                            onCollectionClick = { collection ->
                                // TODO: Navigate to collection detail
                                println("Navigate to collection: ${collection.name}")
                            }
                        )
                    }

                    if (location.images.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        AdventurePhotosCarousel(
                            images = location.images,
                            // Enable these when edit functionality is implemented
                            // onAddPhoto = { /* TODO: Implement add photo */ },
                            // onDeletePhoto = { image -> /* TODO: Implement delete photo */ }
                        )
                    }

                    AboutSection(description = location.description)

                    val lat = location.latitude
                    val lon = location.longitude

                    if (!lat.isNullOrBlank() && !lon.isNullOrBlank()) {
                        MapSection(
                            latitude = lat,
                            longitude = lon,
                            location = location.location,
                            onOpenMap = onOpenMap
                        )
                    }

                    location.link?.let { link ->
                        if (link.isNotEmpty()) {
                            LinkSection(link = link, onOpenLink = onOpenLink)
                        }
                    }

                    AttachmentsSection(
                        attachments = location.attachments,
                        openingAttachmentId = openingAttachmentId,
                        onOpenAttachment = onOpenAttachment
                    )

                    if (location.visits.isNotEmpty()) {
                        VisitsSection(visits = location.visits)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    CreationInfo(
                        createdAt = location.createdAt,
                        updatedAt = location.updatedAt
                    )
                    
                    // Extra padding at the bottom to ensure background covers everything
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// Previews

private val mockUser = UserDetails(
    uuid = "user123",
    username = "previewUser",
    dateJoined = "2025-01-01T00:00:00Z"
)

private val mockCountry = Country(
    id = 1,
    name = "Spain",
    countryCode = "ES",
    flagUrl = "",
    numRegions = 1,
    numVisits = 1,
    subregion = "Southern Europe",
    capital = "Madrid",
    longitude = -3.703790,
    latitude = 40.416775
)

private val mockRegion = Region(
    id = "region-madrid",
    name = "Community of Madrid",
    countryName = "Spain",
    numCities = 1,
    longitude = -3.703790,
    latitude = 40.416775,
    countryId = 1
)

private val mockCity = City(
    id = "city-madrid",
    name = "Madrid",
    regionName = "Community of Madrid",
    countryName = "Spain",
    longitude = -3.703790,
    latitude = 40.416775,
    regionId = "region-madrid"
)

/**
 * Creates an adventure with multiple images for testing the carousel
 */
private fun createAdventureWithMultipleImages(): Location {
    val images = listOf(
        ContentImage(
            id = "img1",
            image = "https://images.unsplash.com/photo-1571896349842-33c89424de2d",
            isPrimary = true,
            user = "user123"
        ),
        ContentImage(
            id = "img2",
            image = "https://images.unsplash.com/photo-1566073771259-6a8506099945",
            isPrimary = false,
            user = "user123"
        )
    )

    return Location(
        id = "adv1",
        user = mockUser,
        name = "Mountain Adventure",
        description = "An amazing mountain adventure with breathtaking views and challenging trails.",
        rating = 4.5,
        tags = listOf("Hiking", "Nature", "Photography"),
        location = "Rocky Mountains, Colorado",
        isPublic = true,
        collections = emptyList(),
        createdAt = "2025-01-15T10:00:00.000Z",
        updatedAt = "2025-01-20T14:30:00.000Z",
        images = images,
        link = "https://example.com/mountain-adventure",
        longitude = "-105.643240",
        latitude = "39.739236",
        visits = listOf(
            Visit(
                id = "visit1",
                location = "adv1",
                startDate = "2025-08-13T00:00:00Z",
                endDate = "2025-08-13T00:00:00Z",
                notes = "",
                timezone = "Europe/Madrid",
                createdAt = "2025-08-13T00:00:00Z",
                updatedAt = "2025-08-13T00:00:00Z"
            )
        ),
        isVisited = true,
        category = Category(
            id = "cat1",
            name = "hiking",
            displayName = "Hiking",
            icon = "🥾",
            numAdventures = "10"
        ),
        attachments = emptyList(),
        city = mockCity,
        country = mockCountry,
        region = mockRegion
    )
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AdventureDetailScreenLightPreview() {
    val mockCollections = PreviewData.sampleUltraSlimCollections.take(2)
    
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                location = createAdventureWithMultipleImages(),
                collections = mockCollections,
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
                location = createAdventureWithMultipleImages().copy(
                    name = "Night Sky Photography Tour",
                    description = "Experience the wonders of astrophotography.",
                    tags = listOf("Photography", "Astronomy", "Night Tour"),
                    isPublic = false
                ),
                collections = emptyList(),
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
    val hotelBalneario = Location(
        id = "c9cfb44c-536a-492c-87ff-8c3bb5d3eec5",
        user = mockUser,
        name = "Gran Hotel Balneario De Puente Viesgo",
        description = "Spa termal en Cantabria con aguas termales y tratamientos de bienestar",
        rating = 3.4,
        tags = listOf("Spa", "Relax", "Turismo"),
        location = "Puente Viesgo, Cantabria",
        isPublic = false,
        collections = emptyList(),
        createdAt = "2025-03-01T10:00:00.000Z",
        updatedAt = "2025-03-15T14:30:00.000Z",
        images = listOf(
            ContentImage(
                id = "img1",
                image = "https://images.unsplash.com/photo-1571896349842-33c89424de2d",
                isPrimary = true,
                user = "user123"
            )
        ),
        link = "https://www.booking.com/hotel/es/gran-balneario-de-puente-viesgo.es.html",
        longitude = "-3.965588",
        latitude = "43.299242",
        visits = emptyList(),
        isVisited = false,
        category = Category(
            id = "cat-hotel",
            name = "hotel",
            displayName = "Hotel",
            icon = "🏨",
            numAdventures = "5"
        ),
        attachments = emptyList(),
        city = mockCity,
        country = mockCountry,
        region = mockRegion
    )

    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                location = hotelBalneario,
                collections = emptyList(),
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
    val navalagamella = Location(
        id = "2ac911dd-8742-45e6-b105-5c04779e8bea",
        user = mockUser,
        name = "Navalagamella",
        description = "Ruta de los molinos con paisajes naturales y arroyos",
        rating = 2.2,
        tags = listOf("Senderismo", "Naturaleza"),
        location = "Navalagamella, Madrid",
        isPublic = false,
        collections = emptyList(),
        createdAt = "2025-02-20T09:15:00.000Z",
        updatedAt = "2025-03-10T11:45:00.000Z",
        images = listOf(
            ContentImage(
                id = "img2",
                image = "https://images.unsplash.com/photo-1551632811-561732d1e306",
                isPrimary = true,
                user = "user123"
            )
        ),
        link = "https://sendasdeviaje.com/navalagamella-ruta-molinos/",
        longitude = "-4.122708",
        latitude = "40.469059",
        visits = emptyList(),
        isVisited = false,
        category = Category(
            id = "cat-ruta",
            name = "ruta",
            displayName = "Ruta",
            icon = "🏞️",
            numAdventures = "3"
        ),
        attachments = emptyList(),
        city = mockCity,
        country = mockCountry,
        region = mockRegion
    )

    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                location = navalagamella,
                collections = emptyList(),
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
private fun AdventureDetailScreenNoImagePreview() {
    val adventureNoImage = Location(
        id = "test1B",
        user = mockUser,
        name = "test1B",
        description = "Una aventura sin imagen para probar el diseño vacío",
        rating = 5.0,
        tags = listOf("test2", "test3"),
        location = "Antonio Corrales",
        isPublic = false,
        collections = emptyList(),
        createdAt = "2025-01-20T10:00:00.000Z",
        updatedAt = "2025-01-20T14:30:00.000Z",
        images = emptyList(), // Sin imágenes
        link = null,
        longitude = null,
        latitude = null,
        visits = emptyList(),
        isVisited = false,
        category = null,
        attachments = emptyList(),
        city = mockCity,
        country = mockCountry,
        region = mockRegion
    )

    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                location = adventureNoImage,
                collections = emptyList(),
                onBackClick = {},
                onEditClick = {},
                onOpenMap = { _, _ -> },
                onOpenLink = {}
            )
        }
    }
}
