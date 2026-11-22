package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind

/**
 * Everything in the collection that could go on a day, to pick one from.
 *
 * The list is the collection's own contents and nothing else - an itinerary entry can only point
 * at something the collection already holds, so a picker that could reach further would only be
 * offering an error.
 *
 * Things already placed are still listed: the server allows the same place on two days, and on a
 * trip that returns to the same city that is the point.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryItemPicker(
    collection: Collection,
    dayLabel: String,
    onPick: (ItineraryItemKind, String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "Add to $dayLabel",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Anything in this collection can go here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            group("Places", collection.locations.map { it.id to it.name }, ItineraryItemKind.LOCATION, onPick)
            group(
                "Transport",
                collection.transportations.map { it.id to it.name },
                ItineraryItemKind.TRANSPORTATION,
                onPick
            )
            group("Lodging", collection.lodging.map { it.id to it.name }, ItineraryItemKind.LODGING, onPick)
            group("Notes", collection.notes.map { it.id to it.name }, ItineraryItemKind.NOTE, onPick)
            group(
                "Checklists",
                collection.checklists.map { it.id to it.name },
                ItineraryItemKind.CHECKLIST,
                onPick
            )

            if (collection.isEmptyOfItems()) {
                item {
                    Text(
                        text = "This collection has nothing in it yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            item { Column(modifier = Modifier.padding(bottom = 32.dp)) {} }
        }
    }
}

private fun Collection.isEmptyOfItems(): Boolean =
    locations.isEmpty() && transportations.isEmpty() && lodging.isEmpty() &&
        notes.isEmpty() && checklists.isEmpty()

private fun androidx.compose.foundation.lazy.LazyListScope.group(
    title: String,
    entries: List<Pair<String, String>>,
    kind: ItineraryItemKind,
    onPick: (ItineraryItemKind, String) -> Unit
) {
    if (entries.isEmpty()) return
    item {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
    }
    items(entries.size) { index ->
        val (id, name) = entries[index]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPick(kind, id) }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (kind) {
                    ItineraryItemKind.TRANSPORTATION -> Icons.Default.Flight
                    ItineraryItemKind.LODGING -> Icons.Default.Hotel
                    ItineraryItemKind.NOTE -> Icons.AutoMirrored.Filled.Notes
                    ItineraryItemKind.CHECKLIST -> Icons.Default.Checklist
                    else -> Icons.Default.Place
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(text = name, style = MaterialTheme.typography.titleMedium)
        }
    }
}
