package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.datetime.LocalDate

/** Where a collection stands against today: "PLANNED" and the days to go, or how it stands now. */
data class CollectionMoment(val eyebrow: String?, val daysToGo: Int?)

private fun day(iso: String?): LocalDate? =
    iso?.substringBefore('T')?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

/**
 * The header's "PLANNED · 8 DAYS TO GO". The detail screen's [com.desarrollodroide.adventurelog.core.model.Collection]
 * carries no status of its own (the list's slim collection does), so it is worked out here from the
 * dates against [today]. No dates: a folder, which says nothing.
 */
fun collectionMoment(start: String?, end: String?, today: LocalDate): CollectionMoment {
    val from = day(start) ?: return CollectionMoment(null, null)
    val to = day(end) ?: from
    return when {
        today < from -> CollectionMoment("PLANNED", (from.toEpochDays() - today.toEpochDays()).toInt())
        today <= to -> CollectionMoment("HAPPENING NOW", null)
        else -> CollectionMoment("COMPLETED", null)
    }
}

private val MONTHS = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

private fun stamp(date: LocalDate) = "${date.day} ${MONTHS[date.month.ordinal]}"

/** "5 SEP → 9 SEP", as the canvas prints a trip's span; one date when it is one day. */
fun arrowDates(start: String?, end: String?): String? {
    val from = day(start) ?: return null
    val to = day(end)
    return if (to == null || to == from) stamp(from) else "${stamp(from)} → ${stamp(to)}"
}

/**
 * The collection's "+": Material's FAB menu, opening onto the five kinds of thing a collection
 * holds. One button where the canvas draws one; the per-section "+" headings stay for adding from
 * where you are reading.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddToCollectionMenu(
    onAddPlace: () -> Unit,
    onAddTransportation: () -> Unit,
    onAddLodging: () -> Unit,
    onAddNote: () -> Unit,
    onAddChecklist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    BackHandler(expanded) { expanded = false }
    val items: List<Triple<String, ImageVector, () -> Unit>> = listOf(
        Triple("Place", Icons.Outlined.Place, onAddPlace),
        Triple("Transport", Icons.Outlined.DirectionsBus, onAddTransportation),
        Triple("Lodging", Icons.Outlined.Hotel, onAddLodging),
        Triple("Note", Icons.AutoMirrored.Outlined.Notes, onAddNote),
        Triple("Checklist", Icons.Outlined.Checklist, onAddChecklist),
    )
    FloatingActionButtonMenu(
        expanded = expanded,
        modifier = modifier,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = it },
                modifier = Modifier.semantics { contentDescription = "Add to collection" },
            ) {
                val icon = if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                Icon(icon, contentDescription = null, modifier = Modifier.animateIcon({ checkedProgress }))
            }
        },
    ) {
        items.forEach { (label, icon, onClick) ->
            FloatingActionButtonMenuItem(
                onClick = { expanded = false; onClick() },
                text = { Text(label) },
                icon = { Icon(icon, contentDescription = null) },
            )
        }
    }
}
