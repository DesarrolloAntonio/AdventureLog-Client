package com.desarrollodroide.adventurelog.feature.adventures.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.AdventureFilters
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.core.model.SortField
import com.desarrollodroide.adventurelog.core.model.VisitedFilter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdventuresFilterBottomSheet(
    filters: AdventureFilters,
    categories: List<Category>,
    onFiltersChanged: (AdventureFilters) -> Unit,
    onDismiss: () -> Unit,
    onManageCategoriesClick: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var localFilters by remember(filters) { mutableStateOf(filters) }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        localFilters = AdventureFilters()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear filters",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories Section
            CategoryFilterSection(
                selectedCategories = localFilters.categoryNames,
                categories = categories,
                onCategoriesChanged = { categoryNames ->
                    localFilters = localFilters.copy(categoryNames = categoryNames)
                },
                onManageCategoriesClick = onManageCategoriesClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Sort Section
            SortSection(
                sortField = localFilters.sortField,
                sortDirection = localFilters.sortDirection,
                onSortFieldChanged = { field ->
                    localFilters = localFilters.copy(sortField = field)
                },
                onSortDirectionChanged = { direction ->
                    localFilters = localFilters.copy(sortDirection = direction)
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Visited Filter Section
            VisitedFilterSection(
                visitedFilter = localFilters.visitedFilter,
                onVisitedFilterChanged = { filter ->
                    localFilters = localFilters.copy(visitedFilter = filter)
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Include Collections Section
            IncludeCollectionsSection(
                includeCollections = localFilters.includeCollections,
                onIncludeCollectionsChanged = { include: Boolean ->
                    localFilters = localFilters.copy(includeCollections = include)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Apply Button
            Button(
                onClick = {
                    onFiltersChanged(localFilters)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilterSection(
    selectedCategories: List<String>,
    categories: List<Category>,
    onCategoriesChanged: (List<String>) -> Unit,
    onManageCategoriesClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No categories available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategories.contains(category.name),
                        onClick = {
                            if (selectedCategories.contains(category.name)) {
                                onCategoriesChanged(selectedCategories - category.name)
                            } else {
                                onCategoriesChanged(selectedCategories + category.name)
                            }
                        },
                        label = {
                            Text(category.displayName)
                        },
                        leadingIcon = if (selectedCategories.contains(category.name)) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SortSection(
    sortField: SortField,
    sortDirection: SortDirection,
    onSortFieldChanged: (SortField) -> Unit,
    onSortDirectionChanged: (SortDirection) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Sort",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sort Direction
        Text(
            text = "Sort direction",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SortDirectionChip(
                text = "Ascending",
                isSelected = sortDirection == SortDirection.ASCENDING,
                onClick = { onSortDirectionChanged(SortDirection.ASCENDING) }
            )
            SortDirectionChip(
                text = "Descending",
                isSelected = sortDirection == SortDirection.DESCENDING,
                onClick = { onSortDirectionChanged(SortDirection.DESCENDING) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sort Field
        Text(
            text = "Sort by",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column {
            SortFieldOption(
                label = "Updated",
                isSelected = sortField == SortField.UPDATED_AT,
                onClick = { onSortFieldChanged(SortField.UPDATED_AT) }
            )
            SortFieldOption(
                label = "Name",
                isSelected = sortField == SortField.NAME,
                onClick = { onSortFieldChanged(SortField.NAME) }
            )
            SortFieldOption(
                label = "Date",
                isSelected = sortField == SortField.CREATED_AT,
                onClick = { onSortFieldChanged(SortField.CREATED_AT) }
            )
            SortFieldOption(
                label = "Rating",
                isSelected = sortField == SortField.RATING,
                onClick = { onSortFieldChanged(SortField.RATING) }
            )
        }
    }
}

@Composable
private fun SortDirectionChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (!isSelected) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        } else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun IncludeCollectionsSection(
    includeCollections: Boolean,
    onIncludeCollectionsChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Sources",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onIncludeCollectionsChanged(!includeCollections) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = includeCollections,
                onCheckedChange = onIncludeCollectionsChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Include collection adventures",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun SortFieldOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun VisitedFilterSection(
    visitedFilter: VisitedFilter,
    onVisitedFilterChanged: (VisitedFilter) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Visited",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VisitedFilterChip(
                text = "All",
                isSelected = visitedFilter == VisitedFilter.ALL,
                onClick = { onVisitedFilterChanged(VisitedFilter.ALL) }
            )
            VisitedFilterChip(
                text = "Visited",
                isSelected = visitedFilter == VisitedFilter.VISITED,
                onClick = { onVisitedFilterChanged(VisitedFilter.VISITED) }
            )
            VisitedFilterChip(
                text = "Not visited",
                isSelected = visitedFilter == VisitedFilter.NOT_VISITED,
                onClick = { onVisitedFilterChanged(VisitedFilter.NOT_VISITED) }
            )
        }
    }
}

@Composable
private fun VisitedFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (!isSelected) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        } else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

