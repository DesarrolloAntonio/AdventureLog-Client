package com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.locations.model.VisitedFilter

/**
 * All / Visited / Planned as one connected button group, each with its count of the whole
 * library ([counts], not the page on screen). Planned is what is not yet visited. Without counts
 * the buttons still filter, by name alone.
 *
 * Connected toggle buttons in a Row, not the Expressive ButtonGroup: that one's measure policy
 * crashed Shiori's list (maxWidth < minWidth), and this sits right above a list.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VisitedFilterGroup(
    counts: UserStats?,
    selected: VisitedFilter,
    onSelect: (VisitedFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        Triple(VisitedFilter.ALL, "All", counts?.locationCount),
        Triple(VisitedFilter.VISITED, "Visited", counts?.visitedLocationCount),
        Triple(
            VisitedFilter.NOT_VISITED,
            "Planned",
            counts?.let { (it.locationCount - it.visitedLocationCount).coerceAtLeast(0) },
        ),
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, (filter, label, count) ->
            ToggleButton(
                checked = selected == filter,
                onCheckedChange = { if (it) onSelect(filter) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                // The canvas: the chosen one in primary, the others in the secondary container.
                colors = ToggleButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .semantics {
                        contentDescription = if (count == null) label else {
                            "$label, $count ${if (count == 1) "place" else "places"}"
                        }
                    },
            ) {
                Text(
                    text = if (count == null) label else "$label $count",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
