package com.desarrollodroide.adventurelog.feature.detail.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.AdventureImage
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.detail.ui.screen.AdventureDetailScreen

/**
 * Provides previews for the AdventureDetailScreen in Android Studio.
 */
@Preview(
    name = "Adventure Detail Screen - Light Theme",
    showBackground = true,
    heightDp = 800
)
@Composable
fun AdventureDetailScreenLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                adventure = PreviewData.adventures[0],
                onBackClick = {},
                onEditClick = {},
                onOpenMap = { _, _ -> },
                onOpenLink = {}
            )
        }
    }
}

/**
 * Preview of AdventureDetailScreen with dark theme
 */
@Preview(
    name = "Adventure Detail Screen - Dark Theme",
    showBackground = true,
    heightDp = 800
)
@Composable
fun AdventureDetailScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureDetailScreen(
                adventure = PreviewData.adventures[1],
                onBackClick = {},
                onEditClick = {},
                onOpenMap = { _, _ -> },
                onOpenLink = {}
            )
        }
    }
}

/**
 * Preview of AdventureDetailScreen with Hotel Balneario (based on PDF example)
 */
@Preview(
    name = "Hotel Balneario Detail Preview",
    showBackground = true,
    heightDp = 800
)
@Composable
fun HotelBalnearioDetailPreview() {
    // Create a custom adventure based on the PDF example
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

/**
 * Preview of AdventureDetailScreen with Navalagamella (based on PDF example)
 */
@Preview(
    name = "Navalagamella Detail Preview",
    showBackground = true,
    heightDp = 800
)
@Composable
fun NavalagamellaDetailPreview() {
    // Create a custom adventure based on the second PDF example
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
