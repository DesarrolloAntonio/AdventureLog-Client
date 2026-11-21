package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.material3.Switch
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.map.ui.state.MapFilters

@OptIn(ExperimentalMaterial3Api::class)
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // No drag handle of its own: ModalBottomSheet draws one, and the sheet showed two
            // stacked bars (QA 06, screenshot).
            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "View Options",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // Categories, as the web keeps them - with how many places each holds, so it is
            // clear which are worth narrowing to.
            if (categoryCounts.isNotEmpty()) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 12.dp)
                ) {
                    categoryCounts.forEach { (name, count) ->
                        FilterChip(
                            selected = name in filters.selectedCategories,
                            onClick = { onToggleCategory(name) },
                            label = { Text("$name ($count)") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Layers",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // Filter options section
            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Visited filter
                FilterOption(
                    icon = Icons.Default.CheckCircle,
                    title = "Visited",
                    count = filters.visitedCount,
                    isSelected = filters.showVisited,
                    onClick = onToggleVisited,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Planned filter
                FilterOption(
                    icon = Icons.Default.Schedule,
                    title = "Planned",
                    count = filters.plannedCount,
                    isSelected = filters.showPlanned,
                    onClick = onTogglePlanned,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Regions filter
                FilterOption(
                    icon = Icons.Default.Map,
                    title = "Visited regions",
                    count = filters.regionCount,
                    isSelected = filters.showRegions,
                    onClick = onToggleShowRegions,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(2.dp))

                FilterOption(
                    icon = Icons.Default.LocationCity,
                    title = "Visited cities",
                    count = filters.cityCount,
                    isSelected = filters.showCities,
                    onClick = onToggleShowCities,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Active filters summary
            val activeFilters = listOf(
                if (filters.showVisited) "Visited" else null,
                if (filters.showPlanned) "Planned" else null,
                if (filters.showRegions) "Regions" else null
            ).filterNotNull()

            if (activeFilters.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active Filters",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = activeFilters.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(activeFilters.size.toString())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Apply button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Apply Filters",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    // A layer is on or off, so the row is a switch: it says so to a screen reader, and the tick
    // that used to lead the row no longer sits beside the Visited layer's own tick icon (QA 06,
    // screenshot: "✓ ✓ Visited").
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .toggleable(value = isSelected, role = Role.Switch, onValueChange = { onClick() }),
        color = if (isSelected) {
            color.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Title and count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
            
            // Count badge
            if (count > 0) {
                Badge(
                    containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Switch(checked = isSelected, onCheckedChange = null)
        }
    }
}
