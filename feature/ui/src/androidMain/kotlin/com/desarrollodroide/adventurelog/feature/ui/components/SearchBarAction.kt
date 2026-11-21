package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * The button that stands next to the search field - filters, sorting, the map.
 *
 * It matches the field's height and fill so the two read as one control rather than a pill with a
 * loose icon beside it, and it fills in when it is doing something, which is the only way to tell
 * from across the room that a filter is on. [badgeCount] says how many rather than merely that
 * there are some.
 */
@Composable
fun SearchBarAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    badgeCount: Int? = null
) {
    Box(modifier = modifier.size(52.dp)) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (active) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
                tint = if (active) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        if (badgeCount != null && badgeCount > 0) {
            Badge(
                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(badgeCount.toString())
            }
        } else if (active) {
            Badge(
                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp),
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
