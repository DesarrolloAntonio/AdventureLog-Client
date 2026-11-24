package com.desarrollodroide.adventurelog.feature.world.ui.screen

import com.desarrollodroide.adventurelog.feature.world.ui.components.CountryRow
import com.desarrollodroide.adventurelog.feature.world.ui.components.WorldFilterGroup
import com.desarrollodroide.adventurelog.feature.world.ui.components.PassportCard
import com.desarrollodroide.adventurelog.feature.world.ui.components.StampsStrip
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import androidx.compose.foundation.layout.Arrangement
import com.desarrollodroide.adventurelog.feature.ui.components.shellAppBarExpanded
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import com.desarrollodroide.adventurelog.feature.ui.components.SearchBarAction
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import com.desarrollodroide.adventurelog.feature.world.ui.state.FilterMode
import com.desarrollodroide.adventurelog.feature.world.ui.state.WorldRegion
import com.desarrollodroide.adventurelog.feature.world.viewmodel.WorldViewModel
import org.koin.compose.viewmodel.koinViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn

@Composable
fun WorldScreen(
    onCountryClick: (String) -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorldViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    WorldScreenContent(
        uiState = uiState,
        isRefreshing = isRefreshing,
        viewModel = viewModel,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onRegionSelected = viewModel::onRegionSelected,
        onShowFilters = { /* TODO: Implement filter dialog */ },
        onCountryClick = onCountryClick,
        onMapClick = onMapClick,
        onRefresh = viewModel::onRefresh,
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
        enabled = shellAppBarExpanded(),
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
                // 250 flag cards is the longest list in the app, and the one that gains most
                // from a second column. Search, progress, filters and cards all live inside the
                // one content column, so they share a left and right edge at every window size.
                ContentColumn {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 120.dp
                    ),
                    // The countries are one segmented group, split by the thin gap; the blocks above
                    // carry their own spacing.
                    verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search bar and filter button as first item
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SimpleSearchBar(
                                searchQuery = uiState.searchQuery,
                                onSearchQueryChange = onSearchQueryChange,
                                placeholder = "Search countries",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(10.dp))
                            SearchBarAction(
                                icon = Icons.Default.Map,
                                contentDescription = "Map view",
                                onClick = onMapClick
                            )
                        }
                    }

                    // Progress section
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        // The passport page, as the canvas draws it. Regions are the countries' visited
                        // regions summed; "planned" countries and a places count have no data here.
                        PassportCard(
                            visitedCountries = uiState.visitedCountriesCount,
                            totalCountries = uiState.totalCountriesCount,
                            regionsVisited = uiState.countries.sumOf { it.numVisits },
                            complete = uiState.completeCountriesCount,
                            partial = uiState.partiallyVisitedCount,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // What has been visited comes first, as stamps - the full list below opens
                    // on 244 empty ones. Not while searching or filtering: the list is the answer.
                    val stamped = uiState.countries.filter { it.numVisits > 0 }
                    if (stamped.isNotEmpty() && uiState.searchQuery.isBlank() && !hasActiveFilters &&
                        uiState.selectedRegion == WorldRegion.ALL
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            StampsStrip(stamped, onClick = onCountryClick, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
                        }
                    }

                    // Filter chips
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Region filter dropdown
                            FilterChipsRow(
                                selectedRegion = uiState.selectedRegion,
                                onRegionSelected = onRegionSelected
                            )
                            
                            WorldFilterGroup(
                                selected = uiState.filterMode,
                                onSelect = viewModel::onFilterModeChanged,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    // Countries or empty state
                    if (uiState.filteredCountries.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyState(
                                searchQuery = uiState.searchQuery,
                                hasFilters = hasActiveFilters || uiState.selectedRegion != WorldRegion.ALL
                            )
                        }
                    } else {
                        val countries = uiState.filteredCountries
                        items(countries.size) { index ->
                            CountryRow(
                                country = countries[index],
                                shape = SegmentedListDefaults.shapeFor(index, countries.size),
                                onClick = onCountryClick
                            )
                        }
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
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        // The same white as the search field directly above it. A grey pill under a white one
        // reads as two unrelated controls that happen to be the same shape.
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
