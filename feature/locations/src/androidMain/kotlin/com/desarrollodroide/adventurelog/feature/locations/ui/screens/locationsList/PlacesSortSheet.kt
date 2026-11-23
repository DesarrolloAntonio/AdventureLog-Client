package com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.feature.locations.model.LocationSortField
import com.desarrollodroide.adventurelog.feature.ui.components.ConnectedChoice
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.FilterSection
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.directionLabels
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.fieldLabel
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.naturalDirection
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * The Sort chip's own sheet: the four orders as one segmented block, the direction in words that
 * fit the order, and every tap applied at once so the list behind already shows it.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PlacesSortSheet(
    sortField: LocationSortField,
    sortDirection: SortDirection,
    onChange: (LocationSortField, SortDirection) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Sort", style = AdventureType.display(34.sp))
                Text(
                    sortSummary(sortField, sortDirection),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
                val fields = LocationSortField.entries
                fields.forEachIndexed { index, field ->
                    SortRow(
                        field = field,
                        selected = field == sortField,
                        shapeIndex = index,
                        count = fields.size,
                        // A new order starts the way it is usually read: names A → Z, the rest
                        // newest or highest first.
                        onClick = { if (field != sortField) onChange(field, naturalDirection(field)) },
                    )
                }
            }

            FilterSection("Direction") {
                val (ascending, descending) = directionLabels(sortField)
                ConnectedChoice(
                    options = listOf(SortDirection.ASCENDING to ascending, SortDirection.DESCENDING to descending),
                    selected = sortDirection,
                    onSelect = { onChange(sortField, it) },
                )
            }

            Button(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text("Done") }
        }
    }
}

@Composable
private fun SortRow(field: LocationSortField, selected: Boolean, shapeIndex: Int, count: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<androidx.compose.ui.graphics.Color>()
    val container by animateColorAsState(if (selected) colors.secondaryContainer else colors.surfaceContainerHigh, effects, label = "row")
    val badge by animateColorAsState(if (selected) colors.primary else colors.surfaceContainerHighest, effects, label = "badge")
    val badgeContent by animateColorAsState(if (selected) colors.onPrimary else colors.onSurfaceVariant, effects, label = "badgeContent")
    Surface(
        shape = SegmentedListDefaults.shapeFor(shapeIndex, count),
        color = container,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .heightIn(min = 64.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = badge, contentColor = badgeContent, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(fieldIcon(field), contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                fieldLabel(field),
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) colors.onSecondaryContainer else colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(MaterialTheme.motionScheme.fastSpatialSpec(), initialScale = 0.3f) + fadeIn(),
                exit = scaleOut(targetScale = 0.3f) + fadeOut(),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = colors.onSecondaryContainer)
            }
        }
    }
}

private fun fieldIcon(field: LocationSortField): ImageVector = when (field) {
    LocationSortField.UPDATED_AT -> Icons.Outlined.Update
    LocationSortField.VISIT_DATE -> Icons.Outlined.Event
    LocationSortField.NAME -> Icons.Default.SortByAlpha
    LocationSortField.RATING -> Icons.Outlined.StarOutline
}

/** "Name · A → Z": the order and its direction in the words the sheet uses. */
internal fun sortSummary(field: LocationSortField, direction: SortDirection): String {
    val (ascending, descending) = directionLabels(field)
    return "${fieldLabel(field)} · ${if (direction == SortDirection.ASCENDING) ascending else descending}"
}
