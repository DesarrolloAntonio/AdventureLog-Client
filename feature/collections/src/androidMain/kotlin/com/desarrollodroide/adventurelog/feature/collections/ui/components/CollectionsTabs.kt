package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The six things a collection holds.
 *
 * A row that scrolls sideways, which is what this was, cuts words in half at both edges - a tab
 * reading "ations" - and hides whichever tabs do not fit behind a gesture nothing announces. Six
 * short labels wrap onto two lines on a phone and sit on one on a tablet, and then every tab is
 * legible and reachable without discovering anything.
 *
 * The counts are the other half of it: which tabs have something in them was previously only
 * discoverable by opening all six.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionsTabs(
    selectedTab: CollectionTab,
    onTabSelected: (CollectionTab) -> Unit,
    modifier: Modifier = Modifier,
    counts: Map<CollectionTab, Int> = emptyMap()
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CollectionTab.entries.forEach { tab ->
            CollectionTabChip(
                text = tab.title,
                count = counts[tab],
                isSelected = selectedTab == tab,
                isEnabled = tab.isEnabled,
                onClick = { if (tab.isEnabled) onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun CollectionTabChip(
    text: String,
    count: Int?,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    // As the canvas draws them: the chosen one dark, a pill, with a check; the rest tonal with
    // tighter corners. The corners and the colour move on the expressive springs.
    val motion = MaterialTheme.motionScheme
    val container by animateColorAsState(
        when {
            !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            isSelected -> MaterialTheme.colorScheme.inverseSurface
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        motion.defaultEffectsSpec(), label = "chip"
    )
    val content = when {
        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        isSelected -> MaterialTheme.colorScheme.inverseOnSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val corner by animateDpAsState(if (isSelected) 20.dp else 12.dp, motion.fastSpatialSpec(), label = "corner")

    Surface(
        modifier = if (isEnabled) Modifier.clickable(onClick = onClick) else Modifier,
        color = container,
        shape = RoundedCornerShape(corner)
    ) {
        Row(
            modifier = Modifier.heightIn(min = 40.dp).padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = content
            )
            // Nothing rather than a zero: an empty tab is still worth opening, to put the first
            // thing in it, but it does not need a badge saying so.
            //
            // In its own pill, because a bare number beside a word reads as part of the label -
            // "Places 3" as a phrase rather than a count. This is the shape the section
            // headings below already use for the same number.
            if (count != null && count > 0) {
                Box(
                    modifier = Modifier
                        .background(content.copy(alpha = 0.18f), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = content
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CollectionsTabsPreview() {
    CollectionsTabs(
        selectedTab = CollectionTab.ALL,
        onTabSelected = {},
        counts = mapOf(
            CollectionTab.LOCATIONS to 3,
            CollectionTab.NOTES to 2,
            CollectionTab.CHECKLISTS to 1
        )
    )
}
