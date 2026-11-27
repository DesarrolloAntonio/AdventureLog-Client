package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

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
    val group: String? = null,
    /** False for a choice this phone cannot take; it stays listed, faded, with [supporting] saying why. */
    val enabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    // Each group is its own segmented block, so a row's corners depend on where it sits in its group.
    val placement = remember(entries) { segmentPlacement(entries) }

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
    ) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = AdventureType.display(34.sp))
            if (note != null) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)
        ) {
            itemsIndexed(entries, key = { _, entry -> entry.value }) { index, entry ->
                val newGroup = entry.group != null && entry.group != entries.getOrNull(index - 1)?.group
                Column {
                    if (newGroup) {
                        Text(
                            text = entry.group.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                start = 8.dp,
                                top = if (index == 0) 0.dp else 16.dp,
                                bottom = 8.dp
                            )
                        )
                    }
                    val (inGroup, groupSize) = placement[index]
                    ChoiceRow(
                        entry = entry,
                        selected = entry.value == selected,
                        shape = SegmentedListDefaults.shapeFor(inGroup, groupSize),
                        onClick = { onSelect(entry.value) }
                    )
                }
            }
        }
    }
}

/** For each entry, its index inside its group and the size of that group. */
private fun segmentPlacement(entries: List<ChoiceEntry>): List<Pair<Int, Int>> {
    val result = ArrayList<Pair<Int, Int>>(entries.size)
    var start = 0
    while (start < entries.size) {
        var end = start + 1
        while (end < entries.size && entries[end].group == entries[start].group) end++
        for (i in start until end) result += (i - start) to (end - start)
        start = end
    }
    return result
}

@Composable
private fun ChoiceRow(entry: ChoiceEntry, selected: Boolean, shape: Shape, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Color>()
    val container by animateColorAsState(
        if (selected) colors.secondaryContainer else colors.surfaceContainerHigh, effects, label = "row"
    )
    val badge by animateColorAsState(
        if (selected) colors.primary else colors.surfaceContainerHighest, effects, label = "badge"
    )
    val badgeContent by animateColorAsState(
        if (selected) colors.onPrimary else colors.onSurfaceVariant, effects, label = "badgeContent"
    )
    Surface(shape = shape, color = container, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .selectable(selected = selected, enabled = entry.enabled, onClick = onClick, role = Role.RadioButton)
                .alpha(if (entry.enabled) 1f else 0.38f)
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (entry.icon != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badge,
                    contentColor = badgeContent,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(entry.icon, contentDescription = null, modifier = Modifier.size(22.dp))
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) colors.onSecondaryContainer else colors.onSurface
                )
                if (entry.supporting != null) {
                    Text(
                        text = entry.supporting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant
                    )
                }
            }
            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(MaterialTheme.motionScheme.fastSpatialSpec(), initialScale = 0.3f) + fadeIn(),
                exit = scaleOut(targetScale = 0.3f) + fadeOut()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = colors.onSecondaryContainer)
            }
        }
    }
}
