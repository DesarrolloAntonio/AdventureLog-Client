package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * One option out of a list, chosen in a sheet rather than a dropdown.
 *
 * A dropdown anchored to a row inside a scrolling card cannot show 25 map styles in six groups
 * without either covering the row it belongs to or scrolling inside a scroll. A sheet has the
 * whole width of the phone and the thumb is already at the bottom of it.
 */
data class ChoiceEntry(
    val value: String,
    val label: String,
    val supporting: String? = null,
    val icon: ImageVector? = null,
    /** Drawn as a heading above this entry when it opens a new group. */
    val group: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsChoiceSheet(
    title: String,
    entries: List<ChoiceEntry>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    note: String? = null
) {
    val listState = rememberLazyListState()

    // Open on the current choice: with 25 styles the selected one is usually below the fold, and
    // a sheet that opens at the top makes you hunt for what you already have.
    LaunchedEffect(entries, selected) {
        val index = entries.indexOfFirst { it.value == selected }
        if (index > 3) listState.scrollToItem(index - 2)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // A 25-entry list fills the screen, and a sheet that reaches the clock has swallowed the
        // status bar along with it.
        modifier = modifier.statusBarsPadding(),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
        )
        if (note != null) {
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            itemsIndexed(entries, key = { _, entry -> entry.value }) { index, entry ->
                val newGroup = entry.group != null && entry.group != entries.getOrNull(index - 1)?.group
                if (newGroup) {
                    Text(
                        text = entry.group.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp
                        )
                    )
                }
                ChoiceRow(
                    entry = entry,
                    selected = entry.value == selected,
                    onClick = { onSelect(entry.value) }
                )
            }
        }
    }
}

@Composable
private fun ChoiceRow(entry: ChoiceEntry, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        if (entry.icon != null) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (entry.supporting != null) {
                Text(
                    text = entry.supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
