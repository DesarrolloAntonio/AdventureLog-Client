package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/** One torn-off stub under a ticket's perforation: a small label over a condensed value. */
data class TicketStub(val label: String, val value: String)

private val TicketShape = RoundedCornerShape(28.dp)
private val NotchRadius = 12.dp

/**
 * A trip drawn as a ticket, the canvas's signature card: photo, name, a perforation with a notch
 * either side, then stubs ("FROM 5 SEP", "TO 9 SEP", "PLACES 2").
 *
 * The notches are holes, so they take the colour of whatever the card sits on: pass it as
 * [notchColor] when that is not the theme's surface.
 */
@Composable
fun TicketCard(
    onClick: () -> Unit,
    photo: @Composable () -> Unit,
    title: String,
    subtitle: String,
    stubs: List<TicketStub>,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null,
    notchColor: Color = MaterialTheme.colorScheme.surface,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = TicketShape,
        color = colors.surfaceContainerLowest,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column {
            // A ratio, not a height: the same card is 380dp wide on a phone and wider on a tablet.
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 7f)
                    .clip(RoundedCornerShape(topStart = 27.dp, topEnd = 27.dp)),
            ) { photo() }

            Row(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = AdventureType.display(30.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                badge?.invoke()
            }

            if (stubs.isNotEmpty()) {
                Perforation(notchColor = notchColor, lineColor = colors.outlineVariant)
                Row(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    stubs.forEach { stub ->
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stub.label,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                                color = colors.onSurfaceVariant,
                                maxLines = 1,
                            )
                            Text(
                                text = stub.value,
                                style = AdventureType.display(26.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** A dashed tear line with a half-circle notch bitten out of each edge. */
@Composable
private fun Perforation(notchColor: Color, lineColor: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(NotchRadius * 2)
            .drawBehind {
                val r = NotchRadius.toPx()
                val y = size.height / 2
                drawLine(
                    color = lineColor,
                    start = Offset(r + 10.dp.toPx(), y),
                    end = Offset(size.width - r - 10.dp.toPx(), y),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                )
                listOf(0f, size.width).forEach { x ->
                    drawCircle(notchColor, radius = r, center = Offset(x, y))
                    drawCircle(lineColor, radius = r, center = Offset(x, y), style = Stroke(1.dp.toPx()))
                }
            },
    )
}

@Preview(widthDp = 380)
@Composable
private fun TicketCardPreview() {
    MaterialTheme {
        TicketCard(
            onClick = {},
            photo = { Box(Modifier.fillMaxWidth().aspectRatio(16f / 7f)) },
            title = "Cinque Terre",
            subtitle = "Five villages on foot",
            stubs = listOf(TicketStub("FROM", "5 SEP"), TicketStub("TO", "9 SEP"), TicketStub("PLACES", "2")),
        )
    }
}
