package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsGroup
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRow

/**
 * "Your data": the web's "update visited regions" and the backup download.
 *
 * A place saved before its coordinates were resolved never had its region counted, so the World
 * tab can read lower than the map looks. The first row asks the server to work the whole lot out
 * again.
 */
@Composable
fun VisitedRegionsCard(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    isBackingUp: Boolean,
    onDownloadBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsGroup(
        title = "Your data",
        segmented = true,
        iconContainer = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
    ) {
        SettingsRow(
            title = "Update visited regions",
            supporting = "Work out which regions and cities your places fall in",
            icon = Icons.Outlined.TravelExplore,
            onClick = onRefresh,
            enabled = !isRefreshing,
            showChevron = false,
            trailing = if (isRefreshing) {
                { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) }
            } else null,
        )
        SettingsRow(
            title = "Download a backup",
            supporting = "Places, visits, collections, media and trails, as one zip",
            icon = Icons.Outlined.CloudDownload,
            onClick = onDownloadBackup,
            enabled = !isBackingUp,
            showChevron = false,
            trailing = if (isBackingUp) {
                { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) }
            } else null,
        )
    }
}
