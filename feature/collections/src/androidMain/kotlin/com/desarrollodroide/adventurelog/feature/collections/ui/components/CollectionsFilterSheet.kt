package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionSortField
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionSortOptions
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionsTab
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * Everything that narrows the list, in the one place you go to narrow it.
 *
 * The screen used to carry two full-width rows of controls above the list - a tab bar for which
 * set of collections, and a chip row for trip status - which is a third of a phone spent on
 * chrome, permanently, for two choices that are rarely made. Both live here now, next to the
 * sorting that was already here, and the screen says which of them are set.
 *
 * Choices apply as they are made and the sheet stays open, so several can be adjusted in one
 * visit; there is nothing to confirm and nothing to lose by closing it.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionsFilterSheet(
    tab: CollectionsTab,
    onTabSelected: (CollectionsTab) -> Unit,
    statusFilter: TripStatus?,
    onStatusFilterChanged: (TripStatus?) -> Unit,
    sortOptions: CollectionSortOptions,
    onSortOptionsChanged: (CollectionSortOptions) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sets = listOf(CollectionsTab.MINE, CollectionsTab.SHARED, CollectionsTab.ARCHIVED)
    val isDefault = tab == CollectionsTab.MINE &&
        statusFilter == null &&
        sortOptions == CollectionSortOptions()

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & sort",
                    style = AdventureType.display(34.sp),
                    modifier = Modifier.weight(1f)
                )
                if (!isDefault) {
                    TextButton(onClick = onReset, shapes = ButtonDefaults.shapes()) { Text("Reset") }
                }
            }

            SheetSection("Show") {
                ConnectedChoice(
                    options = sets.map { it to it.label },
                    // Invites is not one of the three: it shows as Mine, as the tabs here always did.
                    selected = if (tab in sets) tab else CollectionsTab.MINE,
                    onSelect = onTabSelected
                )
            }

            // Status is computed per collection from its dates, and only the user's own
            // collections come back paged; the shared and archived lists are short and whole.
            if (tab == CollectionsTab.MINE) {
                SheetSection("Trip status") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusOptions.forEach { (status, label) ->
                            val on = statusFilter == status
                            // A chip says on or off to a screen reader too, not by colour alone
                            // (QA 07, CA-03).
                            FilterChip(
                                selected = on,
                                onClick = { onStatusFilterChanged(status) },
                                label = { Text(label) },
                                leadingIcon = if (on) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                                } else null,
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }

            SheetSection("Sort by") {
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
                    SortFields.forEachIndexed { index, (field, label) ->
                        SortRow(
                            label = label,
                            icon = sortIcon(field),
                            selected = sortOptions.sortField == field,
                            shapeIndex = index,
                            count = SortFields.size,
                            onClick = { onSortOptionsChanged(sortOptions.copy(sortField = field)) }
                        )
                    }
                }
            }

            SheetSection("Direction") {
                ConnectedChoice(
                    options = listOf(SortDirection.DESCENDING to "Descending", SortDirection.ASCENDING to "Ascending"),
                    selected = sortOptions.sortDirection,
                    onSelect = { onSortOptionsChanged(sortOptions.copy(sortDirection = it)) }
                )
            }
        }
    }
}

/** All the status choices, with "everything" first. */
internal val StatusOptions: List<Pair<TripStatus?, String>> = listOf(
    null to "All",
    TripStatus.FOLDER to "📁 Folder",
    TripStatus.UPCOMING to "🚀 Upcoming",
    TripStatus.IN_PROGRESS to "🎯 In progress",
    // Not "✓": a chosen chip carries a tick of its own, and it read "✓ ✓ Completed" (QA 07).
    TripStatus.COMPLETED to "🏁 Completed"
)

private val SortFields = listOf(
    CollectionSortField.UPDATED_AT to "Recently updated",
    CollectionSortField.START_DATE to "Start date",
    CollectionSortField.NAME to "Name"
)

private fun sortIcon(field: CollectionSortField): ImageVector = when (field) {
    CollectionSortField.UPDATED_AT -> Icons.Outlined.Update
    CollectionSortField.START_DATE -> Icons.Outlined.Event
    CollectionSortField.NAME -> Icons.Default.SortByAlpha
}

/** A section of the sheet: its name in the label voice, then its control. */
@Composable
private fun SheetSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

/**
 * A connected group of toggle buttons, one chosen: the Expressive segmented control. The same as
 * the places filters' own; that one is internal to feature/locations.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <T> ConnectedChoice(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        options.forEachIndexed { index, (value, label) ->
            ToggleButton(
                checked = selected == value,
                onCheckedChange = { if (it) onSelect(value) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.colors(
                    containerColor = colors.surfaceContainerHighest,
                    contentColor = colors.onSurfaceVariant,
                    checkedContainerColor = colors.primary,
                    checkedContentColor = colors.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** One order in the segmented block, as the Places sort sheet draws its rows. */
@Composable
private fun SortRow(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    shapeIndex: Int,
    count: Int,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Color>()
    val container by animateColorAsState(if (selected) colors.secondaryContainer else colors.surfaceContainerHigh, effects, label = "row")
    val badge by animateColorAsState(if (selected) colors.primary else colors.surfaceContainerHighest, effects, label = "badge")
    val badgeContent by animateColorAsState(if (selected) colors.onPrimary else colors.onSurfaceVariant, effects, label = "badgeContent")
    Surface(
        shape = SegmentedListDefaults.shapeFor(shapeIndex, count),
        color = container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .heightIn(min = 64.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = badge, contentColor = badgeContent, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) colors.onSecondaryContainer else colors.onSurface,
                modifier = Modifier.weight(1f)
            )
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
