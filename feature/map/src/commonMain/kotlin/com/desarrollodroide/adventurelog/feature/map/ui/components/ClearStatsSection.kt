package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClearStatsSection(
    visitedCount: Int,
    plannedCount: Int,
    regionCount: Int,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * While the places load, or when they failed to, the counts are unknown - not zero. The card
     * said "0 Visited · 0 Planned · 0 Regions" over the spinner and over "Failed to load places",
     * as if the account were empty (QA 06, screenshots).
     */
    isLoading: Boolean = false,
    placesFailed: Boolean = false,
    /** Regions load on their own; until they have, their count is unknown too. */
    regionsKnown: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        // Opaque: it no longer floats over the map, and at 95% its own shadow showed through as a
        // lighter rectangle inside the card (QA 06, screenshot).
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        icon = Icons.Default.CheckCircle,
                        value = visitedCount.takeUnless { isLoading || placesFailed },
                        label = "Visited",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                    )

                    StatItem(
                        icon = Icons.Default.Schedule,
                        value = plannedCount.takeUnless { isLoading || placesFailed },
                        label = "Planned",
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                    )

                    StatItem(
                        icon = Icons.Default.Public,
                        value = regionCount.takeIf { regionsKnown },
                        label = "Regions",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                FilledTonalIconButton(
                    onClick = onFilterClick,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Info text about location requirement
            Text(
                text = "* Only places with coordinates are shown",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int?,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = color
            )
            Text(
                text = statText(value),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 20.sp
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

/** A count as the card shows it: a dash while it is not known yet, never a zero that is not true. */
internal fun statText(value: Int?): String = value?.toString() ?: "–"
