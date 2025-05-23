package com.desarrollodroide.adventurelog.feature.adventures.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem

/**
 * Preview content that mimics AdventureListScreen without ViewModel dependency
 */
@Composable
private fun AdventureListPreviewContent(
    adventures: List<Adventure> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onAdventureClick: (Adventure) -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: $errorMessage")
                }
            }
            adventures.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No adventures yet")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(adventures) { adventure ->
                        AdventureItem(
                            adventure = adventure,
                            onClick = { onAdventureClick(adventure) }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

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
            AdventureListPreviewContent(
                adventures = PreviewData.adventures,
                onAdventureClick = {}
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
            AdventureListPreviewContent(
                adventures = PreviewData.adventures,
                onAdventureClick = {}
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
            AdventureListPreviewContent(
                adventures = listOf(PreviewData.adventures[0]),
                onAdventureClick = {}
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
            AdventureListPreviewContent(
                adventures = emptyList(),
                onAdventureClick = {}
            )
        }
    }
}

/**
 * Preview of AdventureListScreen with loading state
 */
@Preview(
    name = "Adventure List Screen - Loading",
    showBackground = true,
    heightDp = 400
)
@Composable
fun AdventureListScreenLoadingPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureListPreviewContent(
                isLoading = true,
                onAdventureClick = {}
            )
        }
    }
}

/**
 * Preview of AdventureListScreen with error state
 */
@Preview(
    name = "Adventure List Screen - Error",
    showBackground = true,
    heightDp = 400
)
@Composable
fun AdventureListScreenErrorPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            AdventureListPreviewContent(
                errorMessage = "Failed to load adventures",
                onAdventureClick = {}
            )
        }
    }
}