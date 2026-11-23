package com.desarrollodroide.adventurelog.feature.locations.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.feature.locations.model.LocationFilters
import com.desarrollodroide.adventurelog.feature.locations.model.LocationSortField
import com.desarrollodroide.adventurelog.feature.locations.model.VisitedFilter
import com.desarrollodroide.adventurelog.feature.locations.ui.components.categories.ManageCategoriesDialog
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.CategoryFilterChips
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.CollectionsSwitchRow
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.FilterSection
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.ManageCategoriesButton
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.SortChoice
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.VisitChoice
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.naturalDirection
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel.CategoriesState
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsFilterBottomSheet(
    filters: LocationFilters,
    categoriesState: CategoriesState,
    onFiltersChanged: (LocationFilters) -> Unit,
    onDismiss: () -> Unit,
    onManageCategoriesClick: () -> Unit = {},
    onRetryLoadCategories: () -> Unit = {},
    onAddCategory: ((String, String) -> Unit)? = null,
    onUpdateCategory: ((String, String, String) -> Unit)? = null,
    onDeleteCategory: ((String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var localFilters by remember(filters) { mutableStateOf(filters) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        contentWindowInsets = { androidx.compose.foundation.layout.WindowInsets(0) }
    ) {
        LocationsFilterContent(
            filters = localFilters,
            categoriesState = categoriesState,
            onFiltersChanged = { localFilters = it },
            onApply = {
                onFiltersChanged(localFilters)
                onDismiss()
            },
            onCancel = onDismiss,
            onManageCategoriesClick = { showManageCategoriesDialog = true },
            onRetryLoadCategories = onRetryLoadCategories
        )
    }
    
    // Manage Categories Dialog
    if (showManageCategoriesDialog) {
        ManageCategoriesDialog(
            categoriesState = categoriesState,
            onDismiss = { showManageCategoriesDialog = false },
            onAddCategory = { name, icon ->
                onAddCategory?.invoke(name, icon)
                onManageCategoriesClick()
            },
            onEditCategory = { categoryId, name, icon ->
                onUpdateCategory?.invoke(categoryId, name, icon)
                onManageCategoriesClick()
            },
            onDeleteCategory = { categoryId ->
                onDeleteCategory?.invoke(categoryId)
                onManageCategoriesClick()
            },
            onRetryLoadCategories = onRetryLoadCategories
        )
    }
}

/**
 * The place list's filters: a display title with how many are on and a reset, then what to show,
 * how to sort and which categories, each as Expressive controls instead of cards of radio
 * buttons, and Apply pinned under them.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LocationsFilterContent(
    filters: LocationFilters,
    categoriesState: CategoriesState,
    onFiltersChanged: (LocationFilters) -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit,
    onManageCategoriesClick: () -> Unit = {},
    onRetryLoadCategories: () -> Unit = {}
) {
    val active = getActiveFiltersCount(filters)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Filters", style = AdventureType.display(34.sp))
                Text(
                    text = activeFiltersLabel(active),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalIconButton(
                onClick = { onFiltersChanged(LocationFilters()) },
                enabled = active > 0,
                shapes = IconButtonDefaults.shapes(),
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset filters")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            FilterSection("Show") {
                VisitChoice(filters.visitedFilter) { onFiltersChanged(filters.copy(visitedFilter = it)) }
            }
            FilterSection("Sort by") {
                SortChoice(
                    sortField = filters.sortField,
                    sortDirection = filters.sortDirection,
                    onFieldChange = { onFiltersChanged(filters.copy(sortField = it, sortDirection = naturalDirection(it))) },
                    onDirectionChange = { onFiltersChanged(filters.copy(sortDirection = it)) },
                )
            }
            FilterSection(
                title = if (filters.categoryNames.isEmpty()) "Categories" else "Categories · ${filters.categoryNames.size} chosen",
                trailing = if (categoriesState is CategoriesState.Success) {
                    { ManageCategoriesButton(onManageCategoriesClick) }
                } else null,
            ) {
                CategoryFilterChips(
                    selected = filters.categoryNames,
                    categoriesState = categoriesState,
                    onChange = { onFiltersChanged(filters.copy(categoryNames = it)) },
                    onRetry = onRetryLoadCategories,
                )
            }
            CollectionsSwitchRow(filters.includeCollections) { onFiltersChanged(filters.copy(includeCollections = it)) }
        }

        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) { Text("Cancel") }
                    Button(
                        onClick = onApply,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Apply", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

private fun activeFiltersLabel(count: Int): String = when (count) {
    0 -> "No filters applied"
    1 -> "1 active filter"
    else -> "$count active filters"
}

private fun getActiveFiltersCount(filters: LocationFilters): Int {
    var count = 0
    if (filters.categoryNames.isNotEmpty()) count++
    if (filters.sortField != LocationSortField.UPDATED_AT) count++
    if (filters.sortDirection != SortDirection.DESCENDING) count++
    if (filters.visitedFilter != VisitedFilter.ALL) count++
    // Counting this when it is *on* was right while off was the default. It is on by default
    // now, so an untouched sheet announced one active filter, and the reset button could not
    // clear it.
    if (!filters.includeCollections) count++
    return count
}

@Preview
@Composable
fun LocationsFilterBottomSheetPreview() {
    val mockFilters = LocationFilters(
        categoryNames = listOf("Hiking", "Camping"),
        sortField = LocationSortField.NAME,
        sortDirection = SortDirection.ASCENDING,
        visitedFilter = VisitedFilter.VISITED,
        includeCollections = true
    )

    MaterialTheme {
        Surface {
            LocationsFilterContent(
                filters = mockFilters,
                categoriesState = CategoriesState.Success(emptyList()),
                onFiltersChanged = {},
                onApply = {},
                onCancel = {},
                onManageCategoriesClick = {},
                onRetryLoadCategories = {}
            )
        }
    }
}
