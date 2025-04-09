package com.desarrollodroide.adventurelog.feature.collections.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionList

/**
 * Provides previews for the CollectionsScreen in Android Studio.
 */
@Preview(
    name = "Collections List Screen - Light Theme",
    showBackground = true,
    heightDp = 800
)
@Composable
fun CollectionsScreenLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            CollectionList(
                collections = PreviewData.collections,
                onCollectionClick = {}
            )
        }
    }
}

/**
 * Preview of CollectionsScreen with dark theme
 */
@Preview(
    name = "Collections List Screen - Dark Theme",
    showBackground = true,
    heightDp = 800
)
@Composable
fun CollectionsScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            CollectionList(
                collections = PreviewData.collections,
                onCollectionClick = {}
            )
        }
    }
}

/**
 * Preview of CollectionsScreen with just one item
 */
@Preview(
    name = "Collections List Screen - Single Item",
    showBackground = true,
    heightDp = 400
)
@Composable
fun CollectionsScreenSingleItemPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            CollectionList(
                collections = listOf(PreviewData.collections[0]),
                onCollectionClick = {}
            )
        }
    }
}

/**
 * Preview of CollectionsScreen with empty list
 */
@Preview(
    name = "Collections List Screen - Empty",
    showBackground = true,
    heightDp = 400
)
@Composable
fun CollectionsScreenEmptyPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            CollectionList(
                collections = emptyList(),
                onCollectionClick = {}
            )
        }
    }
}

/**
 * Preview with data from the provided JSON
 * Showing collections from different regions in Spain
 */
@Preview(
    name = "Collections List Screen - Spain Regions",
    showBackground = true,
    heightDp = 800
)
@Composable
fun CollectionsScreenSpainRegionsPreview() {
    // Creating collections based on the JSON data
    val spainRegionsCollections = listOf(
        com.desarrollodroide.adventurelog.core.model.Collection(
            id = "cdcd3ecc-215f-4fdf-a748-94b95e8956a4",
            description = "Rutas y lugares de interés en Álava",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Álava",
            isPublic = false,
            adventures = PreviewData.adventures.take(2),  // Just using some placeholder adventures
            createdAt = "2025-01-30T07:21:07.230845Z",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2025-01-30T07:21:07.230885Z",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        ),
        com.desarrollodroide.adventurelog.core.model.Collection(
            id = "1fa47722-b98b-4c58-ae45-a0a10f78e162",
            description = "Destinos populares en la sierra del Segura",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Albacete",
            isPublic = false,
            adventures = PreviewData.adventures.take(3),
            createdAt = "2025-01-30T15:57:27.605536Z",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2025-01-30T15:57:27.605575Z",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        ),
        com.desarrollodroide.adventurelog.core.model.Collection(
            id = "d64ccfe8-918a-4e3c-8199-82ede3ec3e57",
            description = "Playas y destinos costeros",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Alicante",
            isPublic = false,
            adventures = emptyList(),
            createdAt = "2025-02-09T12:21:01.829885Z",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2025-02-09T12:21:01.829925Z",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        ),
        com.desarrollodroide.adventurelog.core.model.Collection(
            id = "239c3a34-3b6c-46f8-b06e-764f0b5dac53",
            description = "Pueblos pintorescos de Teruel",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Teruel",
            isPublic = false,
            adventures = PreviewData.adventures.take(1),
            createdAt = "2025-02-15T12:41:12.529110Z",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2025-02-15T12:41:12.529149Z",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        )
    )
    
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            CollectionList(
                collections = spainRegionsCollections,
                onCollectionClick = {}
            )
        }
    }
}