package com.desarrollodroide.adventurelog.feature.adventures.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.AdventureFilters
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.core.model.SortField
import com.desarrollodroide.adventurelog.core.model.VisitedFilter
import com.desarrollodroide.adventurelog.feature.adventures.ui.components.categories.ManageCategoriesDialog
import com.desarrollodroide.adventurelog.feature.adventures.ui.components.filters.CategoryFilterSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.components.filters.SortSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.components.filters.VisitedFilterSection
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AdventuresViewModel.CategoriesState
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventuresFilterBottomSheet(
    filters: AdventureFilters,
    categoriesState: CategoriesState,
    onFiltersChanged: (AdventureFilters) -> Unit,
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { androidx.compose.foundation.layout.WindowInsets(0) }
    ) {
        AdventuresFilterContent(
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

@Composable
internal fun AdventuresFilterContent(
    filters: AdventureFilters,
    categoriesState: CategoriesState,
    onFiltersChanged: (AdventureFilters) -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit,
    onManageCategoriesClick: () -> Unit = {},
    onRetryLoadCategories: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // Header with gradient background
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Adventure Filters",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${getActiveFiltersCount(filters)} active filters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(
                    onClick = {
                        onFiltersChanged(AdventureFilters())
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Reset filters",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            // Categories Section
            CategoryFilterSection(
                selectedCategories = filters.categoryNames,
                categoriesState = categoriesState,
                onCategoriesChanged = { categoryNames ->
                    onFiltersChanged(filters.copy(categoryNames = categoryNames))
                },
                onManageCategoriesClick = onManageCategoriesClick,
                onRetryLoadCategories = onRetryLoadCategories
            )

            // Sort Section
            SortSection(
                sortField = filters.sortField,
                sortDirection = filters.sortDirection,
                onSortFieldChanged = { field ->
                    onFiltersChanged(filters.copy(sortField = field))
                },
                onSortDirectionChanged = { direction ->
                    onFiltersChanged(filters.copy(sortDirection = direction))
                }
            )

            // Visited Filter Section
            VisitedFilterSection(
                visitedFilter = filters.visitedFilter,
                onVisitedFilterChanged = { filter ->
                    onFiltersChanged(filters.copy(visitedFilter = filter))
                }
            )

            // Include Collections Section
            IncludeCollectionsSection(
                includeCollections = filters.includeCollections,
                onIncludeCollectionsChanged = { include ->
                    onFiltersChanged(filters.copy(includeCollections = include))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Footer with Apply and Cancel buttons - Always visible at bottom
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Filters")
                }
            }
        }
    }
}

@Composable
private fun IncludeCollectionsSection(
    includeCollections: Boolean,
    onIncludeCollectionsChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Collection Adventures",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Include adventures from collections",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = includeCollections,
                onCheckedChange = onIncludeCollectionsChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

private fun getActiveFiltersCount(filters: AdventureFilters): Int {
    var count = 0
    if (filters.categoryNames.isNotEmpty()) count++
    if (filters.sortField != SortField.UPDATED_AT) count++
    if (filters.sortDirection != SortDirection.DESCENDING) count++
    if (filters.visitedFilter != VisitedFilter.ALL) count++
    if (filters.includeCollections) count++
    return count
}

@Preview
@Composable
fun AdventuresFilterBottomSheetPreview() {
    val mockFilters = AdventureFilters(
        categoryNames = listOf("Hiking", "Camping"),
        sortField = SortField.NAME,
        sortDirection = SortDirection.ASCENDING,
        visitedFilter = VisitedFilter.VISITED,
        includeCollections = true
    )

    MaterialTheme {
        Surface {
            AdventuresFilterContent(
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
