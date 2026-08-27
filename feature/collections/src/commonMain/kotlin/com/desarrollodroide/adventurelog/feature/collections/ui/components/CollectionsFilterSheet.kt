package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionSortField
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionSortOptions
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionsTab
import com.desarrollodroide.adventurelog.feature.ui.components.ChipTone
import com.desarrollodroide.adventurelog.feature.ui.components.MetaChip
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedTabs

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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & sort",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (!isDefault) {
                    TextButton(onClick = onReset) { Text("Reset") }
                }
            }

            SheetSection("Show")
            SegmentedTabs(
                options = sets.map { it.label },
                selectedIndex = sets.indexOf(tab).coerceAtLeast(0),
                onSelect = { onTabSelected(sets[it]) }
            )

            // Status is computed per collection from its dates, and only the user's own
            // collections come back paged; the shared and archived lists are short and whole.
            if (tab == CollectionsTab.MINE) {
                SheetSection("Trip status")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusOptions.forEach { (status, label) ->
                        MetaChip(
                            text = label,
                            tone = if (statusFilter == status) ChipTone.ACCENT else ChipTone.NEUTRAL,
                            onClick = { onStatusFilterChanged(status) }
                        )
                    }
                }
            }

            SheetSection("Sort by")
            Column {
                SortFieldRow("Recently updated", sortOptions.sortField == CollectionSortField.UPDATED_AT) {
                    onSortOptionsChanged(sortOptions.copy(sortField = CollectionSortField.UPDATED_AT))
                }
                SortFieldRow("Start date", sortOptions.sortField == CollectionSortField.START_DATE) {
                    onSortOptionsChanged(sortOptions.copy(sortField = CollectionSortField.START_DATE))
                }
                SortFieldRow("Name", sortOptions.sortField == CollectionSortField.NAME) {
                    onSortOptionsChanged(sortOptions.copy(sortField = CollectionSortField.NAME))
                }
            }

            SheetSection("Direction")
            SegmentedTabs(
                options = listOf("Descending", "Ascending"),
                selectedIndex = if (sortOptions.sortDirection == SortDirection.ASCENDING) 1 else 0,
                onSelect = { index ->
                    onSortOptionsChanged(
                        sortOptions.copy(
                            sortDirection = if (index == 1) {
                                SortDirection.ASCENDING
                            } else {
                                SortDirection.DESCENDING
                            }
                        )
                    )
                }
            )
        }
    }
}

/** All the status choices, with "everything" first. */
internal val StatusOptions: List<Pair<TripStatus?, String>> = listOf(
    null to "All",
    TripStatus.FOLDER to "📁 Folder",
    TripStatus.UPCOMING to "🚀 Upcoming",
    TripStatus.IN_PROGRESS to "🎯 In progress",
    TripStatus.COMPLETED to "✓ Completed"
)

@Composable
private fun SheetSection(title: String) {
    Spacer(Modifier.height(24.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SortFieldRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
