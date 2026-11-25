package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

private val ChipShape = RoundedCornerShape(12.dp)

/**
 * A country as a passport stamp: its code, condensed and tracked, then its name. Visited is
 * inked in the primary colour; planned is only a dashed outline, a stamp not yet given.
 */
@Composable
fun PassportChip(
    code: String,
    name: String,
    visited: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val ink = if (visited) colors.onPrimary else colors.onSurfaceVariant
    val outline = colors.outline
    Row(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(ChipShape)
            .then(
                if (visited) {
                    Modifier.background(colors.primary)
                } else {
                    Modifier.drawBehind {
                        val w = 2.dp.toPx()
                        drawRoundRect(
                            color = outline,
                            topLeft = androidx.compose.ui.geometry.Offset(w / 2, w / 2),
                            size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w),
                            cornerRadius = CornerRadius(12.dp.toPx() - w / 2),
                            style = Stroke(width = w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                        )
                    }
                },
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clearAndSetSemantics {
                contentDescription = "$code $name, ${if (visited) "visited" else "planned"}"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(code, style = AdventureType.stamp(), color = ink, maxLines = 1)
        Text(
            name,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
