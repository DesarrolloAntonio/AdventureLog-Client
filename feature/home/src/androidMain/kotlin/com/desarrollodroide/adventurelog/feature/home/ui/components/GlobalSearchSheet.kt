package com.desarrollodroide.adventurelog.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.core.model.SearchHit
import com.desarrollodroide.adventurelog.core.model.searchTypeIcon
import com.desarrollodroide.adventurelog.core.model.searchTypeLabel
import com.desarrollodroide.adventurelog.feature.home.viewmodel.GlobalSearchViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import org.koin.compose.viewmodel.koinViewModel

/**
 * Search across everything the account holds.
 *
 * Each list on this app searches only itself, which is fine when you know where a thing lives and
 * useless when you do not - and the web has had one box for all of it in the navigation bar the
 * whole time. Results arrive ranked and mixed, so they are shown that way rather than sorted into
 * boxes the user would have to look through in turn.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GlobalSearchSheet(
    onOpenLocation: (String) -> Unit,
    onOpenCollection: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<GlobalSearchViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // The ViewModel belongs to the shell and outlives the sheet, so every way of closing it - a
    // result, the scrim, a drag, back - starts the next search from nothing instead of reopening
    // on the last one.
    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) { focus.requestFocus() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            SimpleSearchBar(
                searchQuery = state.query,
                onSearchQueryChange = viewModel::onQueryChange,
                placeholder = "Search everything",
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .focusRequester(focus)
            )

            Spacer(Modifier.height(8.dp))

            when {
                state.error != null -> Message(state.error.orEmpty(), icon = Icons.Outlined.CloudOff)

                state.tooShort -> Message("Keep typing - two letters at least.")

                state.isSearching && state.hits.isEmpty() -> Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }

                state.hasSearched && state.hits.isEmpty() -> Message(
                    "Nothing matches \"${state.query.trim()}\".",
                    icon = Icons.Outlined.SearchOff
                )

                state.hits.isEmpty() -> Message(
                    "Places, collections, cities - anything with a name.",
                    icon = Icons.Outlined.Search
                )

                // One segmented block: the results arrive ranked and mixed, and read as one list.
                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)
                ) {
                    val hits = state.hits
                    itemsIndexed(hits, key = { _, it -> "${it.type}-${it.id}" }) { index, hit ->
                        HitRow(
                            hit = hit,
                            index = index,
                            count = hits.size,
                            onClick = {
                                when (hit.type) {
                                    SearchHit.LOCATION -> {
                                        onDismiss()
                                        onOpenLocation(hit.id)
                                    }

                                    SearchHit.COLLECTION -> {
                                        onDismiss()
                                        onOpenCollection(hit.id, hit.title)
                                    }

                                    // Everything else - a city, a country, a note inside a
                                    // collection - has no screen of its own here yet, so the row
                                    // says what it found and does not pretend to open it.
                                    else -> Unit
                                }
                            }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HitRow(hit: SearchHit, index: Int, count: Int, onClick: () -> Unit) {
    val openable = hit.type == SearchHit.LOCATION || hit.type == SearchHit.COLLECTION
    val colors = MaterialTheme.colorScheme
    val supporting = listOf(searchTypeLabel(hit.type), hit.subtitle)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(" · ")

    Surface(
        shape = SegmentedListDefaults.shapeFor(index, count),
        color = colors.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                // A hit with nowhere to go is not a button: no ripple, nothing for TalkBack to press.
                .then(if (openable) Modifier.clickable(onClick = onClick) else Modifier)
                .heightIn(min = 64.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // The kind of thing it is, as the emoji the web uses, on the row's tinted square.
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (openable) colors.secondaryContainer else colors.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = searchTypeIcon(hit.type), style = MaterialTheme.typography.titleMedium)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    hit.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (supporting.isNotEmpty()) {
                    Text(
                        supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (openable) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/** A line of guidance, with a cookie badge when it is a state of its own (nothing yet, nothing found, offline). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Message(text: String, icon: ImageVector? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (icon != null) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
