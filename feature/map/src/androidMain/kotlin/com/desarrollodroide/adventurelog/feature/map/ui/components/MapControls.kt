package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.feature.ui.components.PlaceRow
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults

/** A count not loaded yet is a dash, a loaded zero a zero: "0" over a spinner read as an empty account (QA 06). */
internal fun statText(value: Int?): String = value?.toString() ?: "\u2013"

/** What the map shows: every place, the visited ones, or the planned ones. */
enum class MapVisibility { ALL, VISITED, PLANNED }

/** The map keeps two switches (show visited, show planned); this is the one choice they make. */
fun mapVisibilityOf(showVisited: Boolean, showPlanned: Boolean): MapVisibility? = when {
    showVisited && showPlanned -> MapVisibility.ALL
    showVisited -> MapVisibility.VISITED
    showPlanned -> MapVisibility.PLANNED
    else -> null
}

/** Which of the two switches to flip to reach [target]: (flip visited, flip planned). */
fun togglesFor(showVisited: Boolean, showPlanned: Boolean, target: MapVisibility): Pair<Boolean, Boolean> {
    val wantVisited = target != MapVisibility.PLANNED
    val wantPlanned = target != MapVisibility.VISITED
    return (showVisited != wantVisited) to (showPlanned != wantPlanned)
}

/**
 * All / Visited / Planned with their counts, as a connected button group floating over the map.
 * Connected toggle buttons in a Row, not the Expressive ButtonGroup (see VisitedFilterGroup).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapVisibilityGroup(
    /** Null while the places are loading or failed: the buttons then say "–". */
    visitedCount: Int?,
    plannedCount: Int?,
    selected: MapVisibility?,
    onSelect: (MapVisibility) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val options = listOf(
        Triple(MapVisibility.ALL, "All", if (visitedCount != null && plannedCount != null) visitedCount + plannedCount else null),
        Triple(MapVisibility.VISITED, "Visited", visitedCount),
        Triple(MapVisibility.PLANNED, "Planned", plannedCount),
    )
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        options.forEachIndexed { index, (choice, label, count) ->
            ToggleButton(
                checked = selected == choice,
                onCheckedChange = { if (it) onSelect(choice) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                // Over a map: a solid surface for the unchosen, the inverse ink for the chosen.
                colors = ToggleButtonDefaults.colors(
                    containerColor = colors.surfaceContainerLowest,
                    contentColor = colors.onSurface,
                    checkedContainerColor = colors.inverseSurface,
                    checkedContentColor = colors.inverseOnSurface,
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .semantics {
                        contentDescription = if (count == null) label else "$label, $count ${if (count == 1) "place" else "places"}"
                    },
            ) {
                Text("$label ${statText(count)}", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/**
 * The card a tapped pin opens, as the canvas draws it: the place as a row, then Directions and
 * Open. Tapping a pin used to open the page at once, which left no way to look at the map around
 * a place without leaving it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapPlaceCard(
    location: Location,
    onOpen: () -> Unit,
    onDirections: (() -> Unit)?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlaceRow(
                    location = location,
                    shape = SegmentedListDefaults.single,
                    onClick = onOpen,
                    showActions = false,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss, shapes = IconButtonDefaults.shapes()) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (onDirections != null) {
                    FilledTonalButton(onClick = onDirections, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Directions, contentDescription = null)
                        Text("Directions", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Button(onClick = onOpen, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                    Text("Open", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
