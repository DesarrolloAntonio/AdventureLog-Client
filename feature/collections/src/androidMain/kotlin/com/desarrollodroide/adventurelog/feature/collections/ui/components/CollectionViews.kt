package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/** The icon a view is drawn with. Lives here, not on [CollectionView], so that enum stays free of Compose. */
internal val CollectionView.icon: ImageVector
    get() = when (this) {
        CollectionView.ITEMS -> Icons.AutoMirrored.Filled.List
        CollectionView.ITINERARY -> Icons.Default.Timeline
        CollectionView.MAP -> Icons.Default.Map
        CollectionView.CALENDAR -> Icons.Default.CalendarMonth
        CollectionView.RECOMMENDATIONS -> Icons.Default.Explore
        CollectionView.STATS -> Icons.Default.BarChart
    }

/**
 * A scrolling row rather than the wrapping one the tabs use.
 *
 * Two wrapping rows stacked would give the screen a header of unpredictable height - four lines on
 * a phone before any content. Each entry here carries an icon, which is also what keeps the two
 * rows telling apart at a glance when they sit together.
 */
@Composable
fun CollectionViewSwitcher(
    selectedView: CollectionView,
    onViewSelected: (CollectionView) -> Unit,
    modifier: Modifier = Modifier,
    views: List<CollectionView> = CollectionView.entries
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        views.forEach { view ->
            CollectionViewChip(
                view = view,
                isSelected = selectedView == view,
                onClick = { onViewSelected(view) }
            )
        }
    }
}

@Composable
private fun CollectionViewChip(
    view: CollectionView,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val container =
        if (isSelected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest
    val content =
        if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = container,
        shape = RoundedCornerShape(percent = 50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = view.icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = view.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = content
            )
        }
    }
}

@Preview
@Composable
private fun CollectionViewSwitcherPreview() {
    CollectionViewSwitcher(selectedView = CollectionView.STATS, onViewSelected = {})
}
