package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.layout
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
    views: List<CollectionView> = CollectionView.entries,
    /** The page margin the row sits in; it scrolls out to the screen's edges through it. */
    bleed: Dp = 0.dp,
) {
    Row(
        modifier = modifier
            // Out over the page's margin, so the last view slides off the screen's edge instead
            // of being sliced at the margin ("Calend"); the padding puts the first one back in line.
            .layout { measurable, constraints ->
                val extra = bleed.roundToPx()
                val placeable = measurable.measure(
                    constraints.copy(maxWidth = constraints.maxWidth + 2 * extra, minWidth = constraints.minWidth)
                )
                layout((placeable.width - 2 * extra).coerceAtLeast(0), placeable.height) { placeable.place(-extra, 0) }
            }
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = bleed),
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

/**
 * A view of the collection as Expressive's toggle button: round while off, its corners tightening
 * as it is chosen (the button's own shape morph), inked dark when on.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CollectionViewChip(
    view: CollectionView,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    ToggleButton(
        checked = isSelected,
        onCheckedChange = { onClick() },
        shapes = ToggleButtonDefaults.shapesFor(40.dp),
        colors = ToggleButtonDefaults.colors(
            containerColor = colors.surfaceContainerHighest,
            contentColor = colors.onSurfaceVariant,
            checkedContainerColor = colors.inverseSurface,
            checkedContentColor = colors.inverseOnSurface,
        ),
        contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
        modifier = Modifier.heightIn(min = 40.dp),
    ) {
        Icon(imageVector = view.icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = view.title, style = MaterialTheme.typography.labelLarge)
    }
}

@Preview
@Composable
private fun CollectionViewSwitcherPreview() {
    CollectionViewSwitcher(selectedView = CollectionView.STATS, onViewSelected = {})
}
