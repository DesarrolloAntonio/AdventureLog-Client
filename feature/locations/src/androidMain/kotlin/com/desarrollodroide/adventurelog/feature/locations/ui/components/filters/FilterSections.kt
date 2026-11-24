package com.desarrollodroide.adventurelog.feature.locations.ui.components.filters

import com.desarrollodroide.adventurelog.feature.ui.components.ConnectedChoice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.feature.locations.model.LocationSortField
import com.desarrollodroide.adventurelog.feature.locations.model.VisitedFilter
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel.CategoriesState

/** A section of the filter sheet: its name in the label voice, an optional action at the end. */
@Composable
internal fun FilterSection(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            trailing?.invoke()
        }
        content()
    }
}

/** All / Visited / Planned, the words the Places screen uses. */
@Composable
internal fun VisitChoice(visitedFilter: VisitedFilter, onChange: (VisitedFilter) -> Unit) {
    ConnectedChoice(
        options = listOf(VisitedFilter.ALL to "All", VisitedFilter.VISITED to "Visited", VisitedFilter.NOT_VISITED to "Planned"),
        selected = visitedFilter,
        onSelect = onChange,
    )
}

/**
 * What to sort by as single-choice chips, then the direction in words that fit the field: "A → Z"
 * for a name, "Newest first" for a date. An "ascending" next to a rating says nothing.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SortChoice(
    sortField: LocationSortField,
    sortDirection: SortDirection,
    onFieldChange: (LocationSortField) -> Unit,
    onDirectionChange: (SortDirection) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Two by two, equal widths: four chips wrapped by their text left "Rating" alone on a row.
        LocationSortField.entries.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { field ->
                    val chosen = field == sortField
                    ToggleButton(
                        checked = chosen,
                        onCheckedChange = { if (it) onFieldChange(field) },
                        shapes = ToggleButtonDefaults.shapesFor(44.dp),
                        colors = ToggleButtonDefaults.colors(
                            containerColor = colors.surfaceContainerHighest,
                            contentColor = colors.onSurfaceVariant,
                            checkedContainerColor = colors.secondaryContainer,
                            checkedContentColor = colors.onSecondaryContainer,
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                    ) {
                        if (chosen) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Box(Modifier.size(6.dp))
                        }
                        Text(fieldLabel(field), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        val (ascending, descending) = directionLabels(sortField)
        ConnectedChoice(
            options = listOf(SortDirection.ASCENDING to ascending, SortDirection.DESCENDING to descending),
            selected = sortDirection,
            onSelect = onDirectionChange,
            icon = { direction ->
                Icon(
                    if (direction == SortDirection.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
        )
    }
}

/** The direction an order is usually read in: names A → Z, dates and ratings newest/highest first. */
internal fun naturalDirection(field: LocationSortField): SortDirection =
    if (field == LocationSortField.NAME || field == LocationSortField.COUNTRY) SortDirection.ASCENDING else SortDirection.DESCENDING

internal fun fieldLabel(field: LocationSortField) = when (field) {
    LocationSortField.UPDATED_AT -> "Recently updated"
    LocationSortField.VISIT_DATE -> "Visit date"
    LocationSortField.NAME -> "Name"
    LocationSortField.RATING -> "Rating"
    LocationSortField.COUNTRY -> "Country"
}

/** Ascending and descending, said the way the field reads. */
internal fun directionLabels(field: LocationSortField): Pair<String, String> = when (field) {
    LocationSortField.NAME, LocationSortField.COUNTRY -> "A → Z" to "Z → A"
    LocationSortField.RATING -> "Lowest first" to "Highest first"
    LocationSortField.UPDATED_AT, LocationSortField.VISIT_DATE -> "Oldest first" to "Newest first"
}

/** Every category a chip with its icon and how many places it holds; several can be on. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CategoryFilterChips(
    selected: List<String>,
    categoriesState: CategoriesState,
    onChange: (List<String>) -> Unit,
    onRetry: () -> Unit,
) {
    when (categoriesState) {
        is CategoriesState.Loading -> Box(Modifier.fillMaxWidth().heightIn(min = 72.dp), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
        is CategoriesState.Error -> Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(categoriesState.message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                OutlinedButton(onClick = onRetry, shapes = ButtonDefaults.shapes()) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Retry", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        is CategoriesState.Success -> if (categoriesState.categories.isEmpty()) {
            Text(
                "No categories yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // No vertical spacing: each chip already carries 8dp of touch margin above and below,
            // and 8dp more set the rows twice as far apart as the chips beside each other.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categoriesState.categories.forEach { category ->
                    val on = category.name in selected
                    val count = category.numAdventures.toIntOrNull() ?: 0
                    FilterChip(
                        selected = on,
                        onClick = { onChange(if (on) selected - category.name else selected + category.name) },
                        label = {
                            Text(
                                buildString {
                                    if (category.icon.isNotBlank()) append(category.icon).append(' ')
                                    append(category.displayName)
                                    if (count > 0) append("  ").append(count)
                                }
                            )
                        },
                        leadingIcon = if (on) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null,
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
            }
        }
    }
}

/** "Manage" at the end of the categories' heading. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ManageCategoriesButton(onClick: () -> Unit) {
    TextButton(onClick = onClick, shapes = ButtonDefaults.shapes()) {
        Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
        Text("Manage", modifier = Modifier.padding(start = 6.dp))
    }
}

/** Whether places that live in collections show too: a settings-style row with a switch. */
@Composable
internal fun CollectionsSwitchRow(checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .toggleable(value = checked, onValueChange = onChange, role = Role.Switch)
                .heightIn(min = 72.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.CollectionsBookmark, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Places in collections", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Show the places that belong to a collection too",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                thumbContent = if (checked) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                } else null,
            )
        }
    }
}
