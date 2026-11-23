package com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList

import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import com.desarrollodroide.adventurelog.core.model.SortDirection
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.locations.model.LocationSortField
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.desarrollodroide.adventurelog.feature.ui.components.SearchBarAction

/**
 * How Places lays out the library: rows with a thumbnail, as the Expressive canvas draws it, or
 * the photograph across the whole width, as the list was before - for browsing by picture.
 */
enum class PlacesView { ROWS, PHOTOS }

private const val Prefs = "places_ui"
private const val Key = "view"

/** The view last chosen and how to change it, kept on this phone across launches - a preference of how to look, not data. */
@Composable
internal fun rememberPlacesView(): Pair<PlacesView, (PlacesView) -> Unit> {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(Prefs, Context.MODE_PRIVATE) }
    var view by remember {
        val saved = prefs.getString(Key, null)
        mutableStateOf(PlacesView.entries.firstOrNull { it.name == saved } ?: PlacesView.ROWS)
    }
    return view to { chosen ->
        view = chosen
        prefs.edit().putString(Key, chosen.name).apply()
    }
}

/** Beside Filters: shows the view it switches to. */
@Composable
internal fun PlacesViewToggle(view: PlacesView, onChange: (PlacesView) -> Unit, modifier: Modifier = Modifier) {
    val (icon, description, next) = when (view) {
        PlacesView.ROWS -> Triple(Icons.Outlined.ViewAgenda, "Show photos", PlacesView.PHOTOS)
        PlacesView.PHOTOS -> Triple(Icons.AutoMirrored.Outlined.ViewList, "Show as a list", PlacesView.ROWS)
    }
    SearchBarAction(icon = icon, contentDescription = description, onClick = { onChange(next) }, modifier = modifier)
}

/** What a sort reads as on its chip. */
internal fun sortLabel(field: LocationSortField): String = when (field) {
    LocationSortField.UPDATED_AT -> "Updated" // short: the chip row has three chips to fit
    LocationSortField.VISIT_DATE -> "Visit date"
    LocationSortField.NAME -> "Name"
    LocationSortField.RATING -> "Rating"
}

/** "Hiking", "Hiking +2", or just "Category" when none is chosen. */
internal fun categoryLabel(names: List<String>): String = when (names.size) {
    0 -> "Category"
    1 -> names.first()
    else -> "${names.first()} +${names.size - 1}"
}

/**
 * The places filters as a row of chips, as the canvas draws them: the count of what is on, the
 * category, the sort. Each opens the filter sheet; one that is set reads as selected.
 */
@Composable
internal fun PlacesFilterChips(
    sheetFilterCount: Int,
    categoryNames: List<String>,
    sortField: LocationSortField,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    sortDirection: SortDirection = SortDirection.DESCENDING,
    onOpenSort: () -> Unit = onOpen,
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = sheetFilterCount > 0,
            onClick = onOpen,
            label = { Text(if (sheetFilterCount > 0) "Filters · $sheetFilterCount" else "Filters") },
            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) },
            shape = RoundedCornerShape(10.dp),
        )
        FilterChip(
            selected = categoryNames.isNotEmpty(),
            onClick = onOpen,
            label = { Text(categoryLabel(categoryNames)) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) },
            shape = RoundedCornerShape(10.dp),
        )
        // Sort has a sheet of its own; the arrow says which way the list runs.
        FilterChip(
            selected = sortField != LocationSortField.UPDATED_AT || sortDirection != SortDirection.DESCENDING,
            onClick = onOpenSort,
            label = { Text("Sort: ${sortLabel(sortField)}") },
            leadingIcon = {
                Icon(
                    if (sortDirection == SortDirection.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
            // The direction arrow leads; a drop-down arrow too pushed the chip off the row.
            shape = RoundedCornerShape(10.dp),
        )
    }
}
