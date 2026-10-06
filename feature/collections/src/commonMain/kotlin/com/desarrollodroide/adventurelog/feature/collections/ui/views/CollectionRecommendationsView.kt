package com.desarrollodroide.adventurelog.feature.collections.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Recommendation
import com.desarrollodroide.adventurelog.core.model.RecommendationCategory
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.RecommendationsUiState

/**
 * What is near the trip that is not in it yet.
 *
 * The search is anchored on one of the collection's own places by default, because that is the
 * question worth asking from inside a collection - "what else is around Machu Picchu" - and it is
 * the only anchor whose coordinates are exact. Typing a name instead asks the server to geocode
 * it, which is the fallback for a trip that has no places yet.
 */
@Composable
fun CollectionRecommendationsView(
    state: RecommendationsUiState,
    anchors: List<Location>,
    onAnchorSelected: (String?) -> Unit,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (RecommendationCategory) -> Unit,
    onRadiusSelected: (Int) -> Unit,
    onSearch: () -> Unit,
    onAdd: (Recommendation) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchCard(
            state = state,
            anchors = anchors,
            onAnchorSelected = onAnchorSelected,
            onQueryChanged = onQueryChanged,
            onCategorySelected = onCategorySelected,
            onRadiusSelected = onRadiusSelected,
            onSearch = onSearch
        )

        state.errorMessage?.let { message ->
            RecommendationCard {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        when {
            state.isSearching -> RecommendationCard {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.results.isNotEmpty() -> state.results.forEach { recommendation ->
                ResultCard(
                    recommendation = recommendation,
                    isAdded = recommendation.id in state.added,
                    isAdding = state.addingId == recommendation.id,
                    onAdd = { onAdd(recommendation) }
                )
            }

            state.errorMessage == null -> EmptyResults(hasSearched = state.hasSearched)
        }
    }
}

@Composable
private fun SearchCard(
    state: RecommendationsUiState,
    anchors: List<Location>,
    onAnchorSelected: (String?) -> Unit,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (RecommendationCategory) -> Unit,
    onRadiusSelected: (Int) -> Unit,
    onSearch: () -> Unit
) {
    RecommendationCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Find places nearby",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (anchors.isNotEmpty()) {
                Text(
                    text = "Look around",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    anchors.forEach { place ->
                        FilterChip(
                            selected = state.anchorLocationId == place.id,
                            onClick = {
                                onAnchorSelected(
                                    // Tapping the chosen one again clears it, which is how you
                                    // get back to searching by name.
                                    if (state.anchorLocationId == place.id) null else place.id
                                )
                            },
                            label = { Text(place.name) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (anchors.isEmpty()) "Where to look" else "Or somewhere else") },
                placeholder = { Text("A city, an address, a landmark") },
                singleLine = true
            )

            Text(
                text = "What to look for",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RecommendationCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.label) }
                    )
                }
            }

            Text(
                text = "How far",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Radii.forEach { metres ->
                    FilterChip(
                        selected = state.radiusMetres == metres,
                        onClick = { onRadiusSelected(metres) },
                        label = { Text(radiusLabel(metres)) }
                    )
                }
            }

            Button(
                onClick = onSearch,
                enabled = !state.isSearching,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(text = "Search", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun ResultCard(
    recommendation: Recommendation,
    isAdded: Boolean,
    isAdding: Boolean,
    onAdd: () -> Unit
) {
    RecommendationCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isAdded && !isAdding, onClick = onAdd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val where = listOfNotNull(
                    recommendation.primaryType?.replace('_', ' '),
                    recommendation.distanceKm?.let { distanceLabel(it) }
                ).joinToString(" · ")
                if (where.isNotBlank()) {
                    Text(
                        text = where,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                recommendation.address?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when {
                isAdding -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                isAdded -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Already added",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                else -> Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyResults(hasSearched: Boolean) {
    RecommendationCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = if (hasSearched) "Nothing found around there"
                else "Nothing searched for yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (hasSearched) "Try a wider radius, or a different kind of place."
                else "Pick one of this trip's places, or type somewhere, and search.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecommendationCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { content() }
}

private val Radii = listOf(1000, 2000, 5000, 10000, 25000)

private fun radiusLabel(metres: Int): String =
    if (metres < 1000) "$metres m" else "${metres / 1000} km"

/**
 * Under a kilometre reads in hundreds of metres, which is a walk; above it, one decimal, because
 * the second one is noise on a figure that came from a straight line between two points anyway.
 */
private fun distanceLabel(km: Double): String {
    if (km < 1.0) {
        val metres = (km * 1000).toInt()
        return "$metres m away"
    }
    val whole = km.toInt()
    val tenths = ((km - whole) * 10).toInt()
    return "$whole.$tenths km away"
}
