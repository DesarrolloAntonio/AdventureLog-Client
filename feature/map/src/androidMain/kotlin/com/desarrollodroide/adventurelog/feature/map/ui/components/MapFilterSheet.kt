package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.map.ui.state.MapFilters
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * What the map shows: categories as chips, then the four layers as one segmented block of
 * switches. Each flip applies at once, so the map behind already shows it.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun MapFilterSheet(
    filters: MapFilters,
    categoryCounts: List<Pair<String, Int>> = emptyList(),
    onToggleCategory: (String) -> Unit = {},
    onToggleVisited: () -> Unit,
    onTogglePlanned: () -> Unit,
    onToggleShowRegions: () -> Unit,
    onToggleShowCities: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        // No drag handle of its own: ModalBottomSheet draws one, and the sheet showed two
        // stacked bars (QA 06, screenshot).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // What is on, said under the title: it used to be a card of its own above the button.
            val activeFilters = listOfNotNull(
                "Visited".takeIf { filters.showVisited },
                "Planned".takeIf { filters.showPlanned },
                "Regions".takeIf { filters.showRegions },
                "Cities".takeIf { filters.showCities },
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("View Options", style = AdventureType.display(34.sp))
                Text(
                    text = if (activeFilters.isEmpty()) "Every layer is off" else activeFilters.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (activeFilters.isEmpty()) colors.onSurfaceVariant else colors.primary,
                )
            }

            // Categories, as the web keeps them - with how many places each holds, so it is
            // clear which are worth narrowing to.
            if (categoryCounts.isNotEmpty()) {
                SheetSection("Categories") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categoryCounts.forEach { (name, count) ->
                            val on = name in filters.selectedCategories
                            FilterChip(
                                selected = on,
                                onClick = { onToggleCategory(name) },
                                label = { Text("$name ($count)") },
                                leadingIcon = if (on) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                                } else null,
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.secondaryContainer,
                                    selectedLabelColor = colors.onSecondaryContainer,
                                    selectedLeadingIconColor = colors.onSecondaryContainer,
                                ),
                            )
                        }
                    }
                }
            }

            SheetSection("Layers") {
                Column(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
                    LayerRow(
                        icon = Icons.Default.CheckCircle,
                        title = "Visited",
                        count = filters.visitedCount,
                        isSelected = filters.showVisited,
                        onClick = onToggleVisited,
                        accent = colors.primary,
                        onAccent = colors.onPrimary,
                        index = 0,
                    )
                    LayerRow(
                        icon = Icons.Default.Schedule,
                        title = "Planned",
                        count = filters.plannedCount,
                        isSelected = filters.showPlanned,
                        onClick = onTogglePlanned,
                        accent = colors.tertiary,
                        onAccent = colors.onTertiary,
                        index = 1,
                    )
                    LayerRow(
                        icon = Icons.Default.Map,
                        title = "Visited regions",
                        count = filters.regionCount,
                        isSelected = filters.showRegions,
                        onClick = onToggleShowRegions,
                        accent = colors.secondary,
                        onAccent = colors.onSecondary,
                        index = 2,
                    )
                    LayerRow(
                        icon = Icons.Default.LocationCity,
                        title = "Visited cities",
                        count = filters.cityCount,
                        isSelected = filters.showCities,
                        onClick = onToggleShowCities,
                        accent = colors.tertiary,
                        onAccent = colors.onTertiary,
                        index = 3,
                    )
                }
            }

            Button(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text("Apply Filters") }
        }
    }
}

/** A part of the sheet: its name in the label voice, then what it holds. */
@Composable
private fun SheetSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

private const val LAYER_COUNT = 4

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LayerRow(
    icon: ImageVector,
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    accent: Color,
    onAccent: Color,
    index: Int,
) {
    val colors = MaterialTheme.colorScheme
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Color>()
    // The square takes the layer's own colour when it is on: the colour it has on the map.
    val badge by animateColorAsState(if (isSelected) accent else colors.surfaceContainerHighest, effects, label = "badge")
    val badgeContent by animateColorAsState(if (isSelected) onAccent else colors.onSurfaceVariant, effects, label = "badgeContent")

    Surface(
        shape = SegmentedListDefaults.shapeFor(index, LAYER_COUNT),
        color = colors.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // A layer is on or off, so the row is a switch: it says so to a screen reader, and the tick
        // that used to lead the row no longer sits beside the Visited layer's own tick icon (QA 06,
        // screenshot: "✓ ✓ Visited").
        Row(
            Modifier
                .toggleable(value = isSelected, role = Role.Switch, onValueChange = { onClick() })
                .heightIn(min = 64.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = badge, contentColor = badgeContent, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (count > 0) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant
                )
            }
            Switch(
                checked = isSelected,
                onCheckedChange = null,
                thumbContent = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                } else null,
            )
        }
    }
}
