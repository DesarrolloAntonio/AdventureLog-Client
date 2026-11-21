package com.desarrollodroide.adventurelog.feature.world.ui.screen

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import com.desarrollodroide.adventurelog.feature.world.viewmodel.CountryDetailViewModel
import com.desarrollodroide.adventurelog.feature.world.viewmodel.RegionRow
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember

/**
 * One country's regions, and which of them have been visited.
 *
 * This screen was a TODO that printed the country code, so tapping any of the 250 cards on the
 * world screen led nowhere. The endpoint and the repository call both existed already; nothing
 * had ever reached them.
 */
@Composable
fun CountryDetailScreen(
    countryCode: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CountryDetailViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(countryCode) { viewModel.load(countryCode) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingDialog(isLoading = true, showMessage = false)

            state.error != null -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.error.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = viewModel::retry) { Text("Try again") }
            }

            else -> ContentColumn {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        // onNavigateBack reached this screen and nothing drew it: the only way
                        // back was the system gesture (QA 05, screenshot).
                        // In a Box: a grid item centres what it holds, and the arrow sat mid-screen.
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            IconButton(onClick = onNavigateBack, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CountryHeader(
                            name = state.country?.name ?: countryCode.uppercase(),
                            capital = state.country?.capital,
                            visited = state.visitedCount,
                            total = state.regions.size
                        )
                    }

                    if (state.regions.size > 8) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SimpleSearchBar(
                                searchQuery = state.searchQuery,
                                onSearchQueryChange = viewModel::onSearchQueryChanged,
                                placeholder = "Search regions",
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            )
                        }
                    }

                    if (state.regions.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "This country has no regions listed.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(state.filtered, key = { it.region.id }) { row ->
                            RegionCard(row, onClick = { viewModel.onRegionToggled(row.region.id) })
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CountryHeader(
    name: String,
    capital: String?,
    visited: Int,
    total: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (!capital.isNullOrBlank()) {
                Text(
                    text = capital,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            Row {
                Text(
                    text = "Regions visited",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f).alignByBaseline()
                )
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = "$visited",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = " / $total",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { if (total > 0) visited.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}

@Composable
private fun RegionCard(row: RegionRow, onClick: () -> Unit) {
    // A checkbox, not a button with an icon that says "Visited": a screen reader now hears the
    // region, whether it is ticked, and that a tap changes it (QA 05, WO-03).
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .toggleable(value = row.visited, role = Role.Checkbox, onValueChange = { onClick() }),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (row.visited) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.RadioButtonUnchecked
                },
                contentDescription = null,
                tint = if (row.visited) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.region.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (row.region.numCities > 0) {
                    Text(
                        text = if (row.region.numCities == 1) {
                            "1 city"
                        } else {
                            "${row.region.numCities} cities"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
