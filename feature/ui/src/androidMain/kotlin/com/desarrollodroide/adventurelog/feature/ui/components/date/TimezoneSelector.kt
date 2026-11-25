package com.desarrollodroide.adventurelog.feature.ui.components.date

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.common.data.TimezoneData
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.components.StandardSearchField
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimezoneDropdown(
    selectedTimezone: String,
    onTimezoneSelected: (String) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Timezone",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    
        OutlinedTextField(
            value = selectedTimezone,
            onValueChange = { },
            placeholder = { 
                Text(
                    text = "Select timezone",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clickable { showBottomSheet = true },
            enabled = false,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = Color.Transparent,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showBottomSheet) {
        TimezoneBottomSheet(
            currentTimezone = selectedTimezone,
            onTimezoneSelected = { timezone ->
                onTimezoneSelected(timezone)
                showBottomSheet = false
            },
            onDismiss = { showBottomSheet = false }
        )
    }
}

/**
 * The zones as single-choice rows: the device's own and the one in use on top as a short block,
 * then every zone, filtered by what is typed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimezoneBottomSheet(
    currentTimezone: String,
    onTimezoneSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val systemTimezone = TimeZone.currentSystemDefault().id

    // Filter timezones based on search query
    val filteredTimezones = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            TimezoneData.allTimezones
        } else {
            TimezoneData.allTimezones.filter { timezone ->
                timezone.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = "Timezone", style = AdventureType.display(34.sp))
                if (currentTimezone.isNotBlank()) {
                    Text(
                        text = currentTimezone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            StandardSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search timezone..."
            )

            // The device's zone, and the one in use when it is another: the two most often wanted.
            val shortcuts = buildList {
                add(Triple(systemTimezone, "System default", Icons.Outlined.PhoneAndroid))
                if (currentTimezone.isNotBlank() && currentTimezone != systemTimezone) {
                    add(Triple(currentTimezone, "Current", Icons.Outlined.History))
                }
            }
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
                shortcuts.forEachIndexed { index, (zone, label, icon) ->
                    TimezoneRow(
                        title = label,
                        supporting = zone,
                        icon = icon,
                        selected = zone == currentTimezone,
                        index = index,
                        count = shortcuts.size,
                        onClick = { onTimezoneSelected(zone) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)
            ) {
                itemsIndexed(filteredTimezones) { index, timezone ->
                    TimezoneRow(
                        title = timezone,
                        supporting = null,
                        icon = null,
                        selected = timezone == currentTimezone,
                        index = index,
                        count = filteredTimezones.size,
                        onClick = { onTimezoneSelected(timezone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimezoneRow(
    title: String,
    supporting: String?,
    icon: ImageVector?,
    selected: Boolean,
    index: Int,
    count: Int,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Color>()
    val container by animateColorAsState(if (selected) colors.secondaryContainer else colors.surfaceContainerHigh, effects, label = "row")
    val badge by animateColorAsState(if (selected) colors.primary else colors.surfaceContainerHighest, effects, label = "badge")
    val badgeContent by animateColorAsState(if (selected) colors.onPrimary else colors.onSurfaceVariant, effects, label = "badgeContent")
    Surface(
        shape = SegmentedListDefaults.shapeFor(index, count),
        color = container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                // A bare zone name is one line; the shortcuts carry a badge and a second line.
                .heightIn(min = if (icon != null) 64.dp else 52.dp)
                .padding(horizontal = if (icon != null) 12.dp else 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (icon != null) {
                Surface(shape = RoundedCornerShape(12.dp), color = badge, contentColor = badgeContent, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = if (icon != null) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    color = if (selected) colors.onSecondaryContainer else colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                supporting?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
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
