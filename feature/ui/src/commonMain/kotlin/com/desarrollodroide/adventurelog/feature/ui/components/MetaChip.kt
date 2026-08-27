package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * How a chip should carry itself, rather than what colour it should be.
 *
 * Callers used to pass their own two colours, so the same fact wore a saturated teal box on a card
 * and a pale container pill on the detail screen, and a 4dp corner radius in an app whose smallest
 * radius is 14. Naming the intent instead means a category looks like a category everywhere.
 */
enum class ChipTone {
    /** A plain label: a tag, a region, a name. */
    NEUTRAL,

    /** The one fact worth noticing first - usually the category. */
    ACCENT,

    /** Something that went well: visited, complete. */
    POSITIVE,

    /** Something to be aware of: private, unvisited. */
    WARNING,

    /**
     * Over a photograph, where no theme colour is legible against every image. A dark scrim with
     * white on it is, and it lets the picture stay the loudest thing on the card.
     */
    ON_IMAGE
}

@Composable
fun MetaChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.NEUTRAL,
    onRemove: (() -> Unit)? = null
) {
    val container = when (tone) {
        ChipTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant
        ChipTone.ACCENT -> MaterialTheme.colorScheme.primaryContainer
        ChipTone.POSITIVE -> MaterialTheme.colorScheme.secondaryContainer
        ChipTone.WARNING -> MaterialTheme.colorScheme.errorContainer
        ChipTone.ON_IMAGE -> Color.Black.copy(alpha = 0.45f)
    }
    val content = when (tone) {
        ChipTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
        ChipTone.ACCENT -> MaterialTheme.colorScheme.onPrimaryContainer
        ChipTone.POSITIVE -> MaterialTheme.colorScheme.onSecondaryContainer
        ChipTone.WARNING -> MaterialTheme.colorScheme.onErrorContainer
        ChipTone.ON_IMAGE -> Color.White
    }

    Surface(
        modifier = modifier,
        color = container,
        shape = RoundedCornerShape(percent = 50)
    ) {
        Row(
            modifier = Modifier.padding(
                start = 12.dp,
                end = if (onRemove != null) 6.dp else 12.dp,
                top = 6.dp,
                bottom = 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = content
            )
            if (onRemove != null) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(content.copy(alpha = 0.12f))
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove $text",
                        modifier = Modifier.size(14.dp),
                        tint = content
                    )
                }
            }
        }
    }
}
