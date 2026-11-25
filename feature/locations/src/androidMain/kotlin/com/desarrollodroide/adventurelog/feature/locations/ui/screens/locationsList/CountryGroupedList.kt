package com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.feature.locations.model.CountryGroup
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn
import com.desarrollodroide.adventurelog.feature.ui.components.PlaceRow
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * Places under their countries, as the canvas draws the list: a heading per country with its flag
 * and how many places it holds, then that country's rows as one segmented block.
 */
@Composable
internal fun CountryGroupedList(
    groups: List<CountryGroup>,
    onPlaceClick: (Location) -> Unit,
    onEdit: (Location) -> Unit,
    onAddPhoto: (Location) -> Unit,
    onDuplicate: (Location) -> Unit,
    onShare: (Location) -> Unit,
    onDelete: (Location) -> Unit,
    onManageCollections: (Location) -> Unit,
) {
    ContentColumn {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap),
        ) {
            groups.forEach { group ->
                item(key = "header-${group.country}", contentType = "header") {
                    CountryHeading(group, Modifier.padding(top = 16.dp, bottom = 8.dp))
                }
                itemsIndexed(group.locations, key = { _, it -> it.id }, contentType = { _, _ -> "place" }) { index, place ->
                    PlaceRow(
                        location = place,
                        shape = SegmentedListDefaults.shapeFor(index, group.locations.size),
                        onClick = { onPlaceClick(place) },
                        onEdit = { onEdit(place) },
                        onAddPhoto = { onAddPhoto(place) },
                        onDuplicate = { onDuplicate(place) },
                        onShare = { onShare(place) },
                        onDelete = { onDelete(place) },
                        onManageCollections = { onManageCollections(place) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryHeading(group: CountryGroup, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        flagOf(group.countryCode)?.let { Text(it, fontSize = 24.sp) }
        Text(
            group.country ?: "Somewhere else",
            style = AdventureType.display(26.sp),
            modifier = Modifier.weight(1f, fill = false),
        )
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
            Text(
                group.locations.size.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            )
        }
    }
}

/** "JP" → 🇯🇵, from the two regional-indicator letters; null for anything that is not a code. */
internal fun flagOf(code: String?): String? {
    val letters = code?.trim()?.uppercase() ?: return null
    if (letters.length != 2 || !letters.all { it in 'A'..'Z' }) return null
    return letters.map { String(Character.toChars(0x1F1E6 + (it - 'A'))) }.joinToString("")
}

/** While the whole library loads for the first time. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CountryGroupsLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingIndicator() }
}
