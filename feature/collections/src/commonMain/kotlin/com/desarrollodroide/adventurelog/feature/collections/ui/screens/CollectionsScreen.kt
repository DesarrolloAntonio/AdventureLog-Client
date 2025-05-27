package com.desarrollodroide.adventurelog.feature.collections.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionItem
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CollectionsScreen(
    onCollectionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${uiState.errorMessage}")
            }
        }
        else -> {
            CollectionList(
                collections = uiState.collections,
                onCollectionClick = onCollectionClick,
                modifier = modifier
            )
        }
    }
}

@Composable
fun CollectionList(
    collections: List<Collection>,
    onCollectionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        collections.forEach { collection ->
            CollectionItem(
                collection = collection,
                onClick = { onCollectionClick(collection.id) }
            )
        }
    }
}

// Previews
@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun CollectionsScreenLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            CollectionList(
                collections = com.desarrollodroide.adventurelog.core.model.preview.PreviewData.collections,
                onCollectionClick = {}
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun CollectionsScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            CollectionList(
                collections = com.desarrollodroide.adventurelog.core.model.preview.PreviewData.collections,
                onCollectionClick = {}
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun CollectionsScreenSpainRegionsPreview() {
    val spainRegionsCollections = listOf(
        Collection(
            id = "cdcd3ecc-215f-4fdf-a748-94b95e8956a4",
            description = "Rutas y lugares de interés en Álava",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Álava",
            isPublic = false,
            adventures = com.desarrollodroide.adventurelog.core.model.preview.PreviewData.adventures.take(2),
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
        Collection(
            id = "1fa47722-b98b-4c58-ae45-a0a10f78e162",
            description = "Destinos populares en la sierra del Segura",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Albacete",
            isPublic = false,
            adventures = com.desarrollodroide.adventurelog.core.model.preview.PreviewData.adventures.take(3),
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
        Collection(
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
        Collection(
            id = "239c3a34-3b6c-46f8-b06e-764f0b5dac53",
            description = "Pueblos pintorescos de Teruel",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Teruel",
            isPublic = false,
            adventures = com.desarrollodroide.adventurelog.core.model.preview.PreviewData.adventures.take(1),
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
