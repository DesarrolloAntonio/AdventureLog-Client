package com.desarrollodroide.adventurelog.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.core.model.SearchHit
import com.desarrollodroide.adventurelog.core.model.searchTypeIcon
import com.desarrollodroide.adventurelog.core.model.searchTypeLabel
import com.desarrollodroide.adventurelog.feature.home.viewmodel.GlobalSearchViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRow
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRowDivider
import org.koin.compose.viewmodel.koinViewModel

/**
 * Search across everything the account holds.
 *
 * Each list on this app searches only itself, which is fine when you know where a thing lives and
 * useless when you do not - and the web has had one box for all of it in the navigation bar the
 * whole time. Results arrive ranked and mixed, so they are shown that way rather than sorted into
 * boxes the user would have to look through in turn.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchSheet(
    onOpenLocation: (String) -> Unit,
    onOpenCollection: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<GlobalSearchViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) { focus.requestFocus() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
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
                state.error != null -> Message(state.error.orEmpty())

                state.tooShort -> Message("Keep typing - two letters at least.")

                state.isSearching && state.hits.isEmpty() -> Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                }

                state.hasSearched && state.hits.isEmpty() -> Message(
                    "Nothing matches \"${state.query.trim()}\"."
                )

                state.hits.isEmpty() -> Message(
                    "Places, collections, cities - anything with a name."
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(state.hits, key = { "${it.type}-${it.id}" }) { hit ->
                        HitRow(
                            hit = hit,
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
                        SettingsRowDivider()
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HitRow(hit: SearchHit, onClick: () -> Unit) {
    val openable = hit.type == SearchHit.LOCATION || hit.type == SearchHit.COLLECTION

    SettingsRow(
        title = hit.title,
        supporting = listOf(searchTypeLabel(hit.type), hit.subtitle)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" · "),
        onClick = if (openable) onClick else null,
        showChevron = openable,
        trailing = {
            Text(text = searchTypeIcon(hit.type), style = MaterialTheme.typography.titleMedium)
        }
    )
}

@Composable
private fun Message(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )
    }
}
