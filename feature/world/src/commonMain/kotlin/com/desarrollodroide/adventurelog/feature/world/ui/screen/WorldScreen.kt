package com.desarrollodroide.adventurelog.feature.world.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.feature.ui.components.ErrorState
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingCard
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import com.desarrollodroide.adventurelog.feature.world.ui.components.CompactProgressSection
import com.desarrollodroide.adventurelog.feature.world.ui.components.CountryCard
import com.desarrollodroide.adventurelog.feature.world.ui.state.FilterMode
import com.desarrollodroide.adventurelog.feature.world.ui.state.WorldRegion
import com.desarrollodroide.adventurelog.feature.world.viewmodel.WorldViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WorldScreen(
    onCountryClick: (String) -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorldViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }

    WorldScreenContent(
        uiState = uiState,
        isRefreshing = isRefreshing,
        viewModel = viewModel,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onRegionSelected = viewModel::onRegionSelected,
        onShowFilters = { /* TODO: Implement filter dialog */ },
        onCountryClick = onCountryClick,
        onMapClick = onMapClick,
        onRefresh = {
            isRefreshing = true
            viewModel.onRefresh()
            isRefreshing = false
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorldScreenContent(
    uiState: com.desarrollodroide.adventurelog.feature.world.ui.state.WorldUiState,
    isRefreshing: Boolean,
    viewModel: WorldViewModel,
    onSearchQueryChange: (String) -> Unit,
    onRegionSelected: (WorldRegion) -> Unit,
    onShowFilters: () -> Unit,
    onCountryClick: (String) -> Unit,
    onMapClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val hasActiveFilters = uiState.filterMode != FilterMode.ALL

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading && uiState.countries.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingCard(
                        message = "Loading countries...",
                        showOverlay = false
                    )
                }
            }
            
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error,
                    onRetry = onRefresh
                )
            }
            
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search bar and filter button as first item
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SimpleSearchBar(
                                searchQuery = uiState.searchQuery,
                                onSearchQueryChange = onSearchQueryChange,
                                onSearchSubmit = { },
                                placeholder = "Search countries...",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(0.dp) // Remove default padding
                            )
                            IconButton(
                                onClick = onMapClick,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Map view",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Progress section
                    item {
                        CompactProgressSection(
                            totalCountries = uiState.totalCountriesCount,
                            visitedCount = uiState.visitedCountriesCount,
                            partiallyVisitedCount = uiState.partiallyVisitedCount,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Filter chips
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Region filter dropdown
                            FilterChipsRow(
                                selectedRegion = uiState.selectedRegion,
                                onRegionSelected = onRegionSelected
                            )
                            
                            // Visit status filter chips
                            VisitStatusFilters(
                                selectedFilter = uiState.filterMode,
                                onFilterSelected = { filterMode ->
                                    viewModel.onFilterModeChanged(filterMode)
                                }
                            )
                        }
                    }

                    // Countries or empty state
                    if (uiState.filteredCountries.isEmpty()) {
                        item {
                            EmptyState(
                                searchQuery = uiState.searchQuery,
                                hasFilters = hasActiveFilters || uiState.selectedRegion != WorldRegion.ALL
                            )
                        }
                    } else {
                        items(uiState.filteredCountries) { country ->
                            CountryCard(
                                country = country,
                                onClick = { onCountryClick(country.countryCode) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    selectedRegion: WorldRegion,
    onRegionSelected: (WorldRegion) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = selectedRegion.displayName,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                WorldRegion.entries.forEach { region ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = region.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            ) 
                        },
                        onClick = {
                            onRegionSelected(region)
                            expanded = false
                        },
                        leadingIcon = if (selectedRegion == region) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitStatusFilters(
    selectedFilter: FilterMode,
    onFilterSelected: (FilterMode) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(FilterMode.entries) { filterMode ->
            val isSelected = selectedFilter == filterMode
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filterMode) },
                label = {
                    Text(
                        text = when (filterMode) {
                            FilterMode.ALL -> "Todo"
                            FilterMode.COMPLETE -> "Completo"
                            FilterMode.PARTIAL -> "Parcial"
                            FilterMode.NOT_VISITED -> "No visitado"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (filterMode) {
                        FilterMode.ALL -> MaterialTheme.colorScheme.primary
                        FilterMode.COMPLETE -> Color(0xFF4CAF50)
                        FilterMode.PARTIAL -> Color(0xFFFFA726)
                        FilterMode.NOT_VISITED -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    selectedLabelColor = when (filterMode) {
                        FilterMode.NOT_VISITED -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> Color.White
                    }
                )
            )
        }
    }
}

@Composable
private fun EmptyState(
    searchQuery: String,
    hasFilters: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = when {
                    searchQuery.isNotEmpty() && hasFilters -> "No countries found for \"$searchQuery\" with current filters"
                    searchQuery.isNotEmpty() -> "No countries found for \"$searchQuery\""
                    hasFilters -> "No countries found with current filters"
                    else -> "No countries available"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = when {
                    hasFilters -> "Try adjusting your filters or search terms"
                    else -> "Try searching with different keywords"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}