package com.desarrollodroide.adventurelog.feature.collections.ui.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionItem

/**
 * Provides previews for the CollectionItem component in Android Studio.
 */
@Preview(
    name = "Collection Item - With Adventures",
    showBackground = true,
    heightDp = 250
)
@Composable
fun CollectionItemWithAdventuresPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CollectionItem(
                collection = PreviewData.collections[1], // Collection with adventures
                onClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * Preview of CollectionItem without adventures
 */
@Preview(
    name = "Collection Item - Empty",
    showBackground = true,
    heightDp = 150
)
@Composable
fun CollectionItemEmptyPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CollectionItem(
                collection = PreviewData.collections[2], // Collection without adventures
                onClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * Preview with data from the provided JSON - Albacete collection
 */
@Preview(
    name = "Collection Item - Albacete",
    showBackground = true,
    heightDp = 250
)
@Composable
fun CollectionItemAlbacetePreview() {
    // Creating a collection based on the JSON data
    val albaceteCollection = Collection(
        id = "1fa47722-b98b-4c58-ae45-a0a10f78e162",
        description = "Descubre la sierra del Segura y sus maravillas naturales",
        userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
        name = "Albacete",
        isPublic = false,
        adventures = PreviewData.adventures.take(3), // Using placeholder adventures
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
    )
    
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CollectionItem(
                collection = albaceteCollection,
                onClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * Preview with data from the provided JSON - Teruel collection
 */
@Preview(
    name = "Collection Item - Teruel",
    showBackground = true,
    heightDp = 250
)
@Composable
fun CollectionItemTeruelPreview() {
    // Creating a collection based on the JSON data
    val teruelCollection = Collection(
        id = "239c3a34-3b6c-46f8-b06e-764f0b5dac53",
        description = "Rutas por el Parrizal de Beceite y pueblos medievales",
        userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
        name = "Teruel",
        isPublic = false,
        adventures = PreviewData.adventures.take(2), // Using placeholder adventures
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
    
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CollectionItem(
                collection = teruelCollection,
                onClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}