package com.desarrollodroide.adventurelog.feature.adventures.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.AdventureListScreen

/**
 * Provides previews for the AdventureListScreen in Android Studio.
 */
@Preview(
    name = "Adventure List Screen - Light Theme",
    showBackground = true,
    heightDp = 800
)
@Composable
fun AdventureListScreenLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureListScreen(
                adventureItems = PreviewData.adventures,
                onOpenDetails = {}
            )
        }
    }
}

/**
 * Preview of AdventureListScreen with dark theme
 */
@Preview(
    name = "Adventure List Screen - Dark Theme",
    showBackground = true,
    heightDp = 800
)
@Composable
fun AdventureListScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureListScreen(
                adventureItems = PreviewData.adventures,
                onOpenDetails = {}
            )
        }
    }
}

/**
 * Preview of AdventureListScreen with just one item
 */
@Preview(
    name = "Adventure List Screen - Single Item",
    showBackground = true,
    heightDp = 400
)
@Composable
fun AdventureListScreenSingleItemPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureListScreen(
                adventureItems = listOf(PreviewData.adventures[0]),
                onOpenDetails = {}
            )
        }
    }
}

/**
 * Preview of AdventureListScreen with empty list
 */
@Preview(
    name = "Adventure List Screen - Empty",
    showBackground = true,
    heightDp = 400
)
@Composable
fun AdventureListScreenEmptyPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureListScreen(
                adventureItems = emptyList(),
                onOpenDetails = {}
            )
        }
    }
}