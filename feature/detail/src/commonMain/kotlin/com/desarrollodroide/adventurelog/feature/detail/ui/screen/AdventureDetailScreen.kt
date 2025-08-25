package com.desarrollodroide.adventurelog.feature.detail.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.AdventureImage
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.feature.detail.ui.components.*
import com.desarrollodroide.adventurelog.feature.detail.viewmodel.AdventureDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

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
        onOpenMap = { lat: String, long: String -> viewModel.openMap(lat, long) },
        onOpenLink = { url: String -> viewModel.openLink(url) }
    )
}

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
                        title = adventure.name,
                        location = adventure.location
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryTags(
                        category = adventure.category,
                        isPublic = adventure.isPublic
                    )

                    if (adventure.images.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        AdventurePhotosCarousel(
                            images = adventure.images,
                            // Enable these when edit functionality is implemented
                            // onAddPhoto = { /* TODO: Implement add photo */ },
                            // onDeletePhoto = { image -> /* TODO: Implement delete photo */ }
                        )
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
                    
                    // Extra padding at the bottom to ensure background covers everything
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
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
                startDate = "2025-08-13T00:00:00Z",
                endDate = "2025-08-13T00:00:00Z",
                notes = "",
                timezone = "Madrid"
            ),
            Visit(
                id = "visit2",
                startDate = "2025-08-31T00:00:00Z",
                endDate = "2025-08-31T00:00:00Z",
                notes = "",
                timezone = "Madrid"
            ),
            Visit(
                id = "visit3",
                startDate = "2025-08-28T06:59:00Z",
                endDate = "2025-08-30T06:59:00Z",
                notes = "",
                timezone = "Madrid"
            ),
            Visit(
                id = "visit4",
                startDate = "2025-08-14T00:00:00Z",
                endDate = "2025-08-14T00:00:00Z",
                notes = "Notas",
                timezone = ""
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
                onOpenMap = { _: String, _: String -> },
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
                onOpenMap = { _: String, _: String -> },
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
        collections = emptyList(),
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
            numAdventures = "5"
        ),
        attachments = emptyList()
    )

    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                adventure = hotelBalneario,
                onBackClick = {},
                onEditClick = {},
                onOpenMap = { _: String, _: String -> },
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
        collections = emptyList(),
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
            numAdventures = "3"
        ),
        attachments = emptyList()
    )

    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                adventure = navalagamella,
                onBackClick = {},
                onEditClick = {},
                onOpenMap = { _: String, _: String -> },
                onOpenLink = {}
            )
        }
    }
}
