package com.desarrollodroide.adventurelog.feature.world.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.FilledIconButton
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
                    // The regions are one segmented group, split by the thin gap.
                    verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        // onNavigateBack reached this screen and nothing drew it: the only way
                        // back was the system gesture (QA 05, screenshot).
                        // In a Box: a grid item centres what it holds, and the arrow sat mid-screen.
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            FilledIconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.padding(top = 4.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CountryHeader(
                            code = countryCode.uppercase(),
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
                        val rows = state.filtered
                        items(rows.size, key = { rows[it].region.id }) { index ->
                            val row = rows[index]
                            RegionCard(
                                row,
                                shape = SegmentedListDefaults.shapeFor(index, rows.size),
                                onClick = { viewModel.onRegionToggled(row.region.id) }
                            )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CountryHeader(
    code: String,
    name: String,
    capital: String?,
    visited: Int,
    total: Int
) {
    val colors = MaterialTheme.colorScheme
    // As the canvas draws it: the country's code as a stamp beside its name, then the regions
    // visited out of all on a dark card, on Expressive's wavy progress.
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(code, style = AdventureType.stamp().copy(fontSize = 26.sp), color = colors.onPrimary)
            }
            Column(Modifier.weight(1f)) {
                Text(text = name, style = AdventureType.display(52.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (!capital.isNullOrBlank()) {
                    Text(text = capital, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.inverseSurface,
            contentColor = colors.inverseOnSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(visited.toString(), style = AdventureType.display(56.sp), color = colors.inversePrimary)
                    Text("/ $total", style = AdventureType.display(26.sp), color = colors.inverseOnSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp))
                    Text("regions visited", style = MaterialTheme.typography.bodyMedium,
                        color = colors.inverseOnSurface.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 9.dp))
                }
                LinearWavyProgressIndicator(
                    progress = { if (total > 0) visited.toFloat() / total else 0f },
                    color = colors.inversePrimary,
                    trackColor = colors.inverseOnSurface.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RegionCard(row: RegionRow, shape: androidx.compose.ui.graphics.Shape, onClick: () -> Unit) {
    // A checkbox, not a button with an icon that says "Visited": a screen reader now hears the
    // region, whether it is ticked, and that a tap changes it (QA 05, WO-03). The pill on the end
    // says it to the eye, as the canvas draws it.
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .toggleable(value = row.visited, role = Role.Checkbox, onValueChange = { onClick() }),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.heightIn(min = 60.dp).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.region.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (row.visited) FontWeight.Bold else FontWeight.Normal,
                    color = if (row.visited) colors.onSurface else colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (row.region.numCities > 0) {
                    Text(
                        text = if (row.region.numCities == 1) "1 city" else "${row.region.numCities} cities",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            VisitedPill(row.visited)
        }
    }
}

/** "✓ Visited" inked, or "Mark visited" outlined; colour and corners move on the springs. */
@Composable
private fun VisitedPill(visited: Boolean) {
    val colors = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val container by animateColorAsState(if (visited) colors.primary else Color.Transparent, motion.defaultEffectsSpec(), label = "pill")
    val corner by animateDpAsState(if (visited) 16.dp else 10.dp, motion.fastSpatialSpec(), label = "corner")
    val ink = if (visited) colors.onPrimary else colors.onSurfaceVariant
    Row(
        Modifier
            .clearAndSetSemantics { }
            .height(32.dp)
            .clip(RoundedCornerShape(corner))
            .background(container)
            .then(if (visited) Modifier else Modifier.border(1.dp, colors.outlineVariant, RoundedCornerShape(corner)))
            .padding(start = if (visited) 8.dp else 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (visited) Icon(Icons.Default.Check, contentDescription = null, tint = ink, modifier = Modifier.size(16.dp))
        Text(if (visited) "Visited" else "Mark visited", style = MaterialTheme.typography.labelLarge, color = ink)
    }
}
