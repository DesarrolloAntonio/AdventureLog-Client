package com.desarrollodroide.adventurelog.feature.adventures.ui.preview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.adventures.adventures.AdventureItem

/**
 * Provides previews for the AdventureItem in Android Studio.
 */
@Preview(
    name = "Adventure Item - Light Theme",
    showBackground = true
)
@Composable
fun AdventureItemLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AdventureItem(
                adventure = PreviewData.adventures[0],
                onOpenDetails = {},
                onEdit = {},
                onRemoveFromCollection = {},
                onDelete = {}
            )
        }
    }
}

/**
 * Preview of AdventureItem with dark theme
 */
@Preview(
    name = "Adventure Item - Dark Theme",
    showBackground = true
)
@Composable
fun AdventureItemDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AdventureItem(
                adventure = PreviewData.adventures[1],
                onOpenDetails = {},
                onEdit = {},
                onRemoveFromCollection = {},
                onDelete = {}
            )
        }
    }
}

/**
 * Preview of AdventureItem with a private adventure
 */
@Preview(
    name = "Adventure Item - Private Adventure",
    showBackground = true
)
@Composable
fun AdventureItemPrivatePreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AdventureItem(
                adventure = PreviewData.adventures[0], // This one is marked as private
                onOpenDetails = {},
                onEdit = {},
                onRemoveFromCollection = {},
                onDelete = {}
            )
        }
    }
}

/**
 * Preview of AdventureItem with a collection tag
 */
@Preview(
    name = "Adventure Item - With Collection",
    showBackground = true
)
@Composable
fun AdventureItemWithCollectionPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AdventureItem(
                adventure = PreviewData.adventures[1], // This one has a collection
                onOpenDetails = {},
                onEdit = {},
                onRemoveFromCollection = {},
                onDelete = {}
            )
        }
    }
}