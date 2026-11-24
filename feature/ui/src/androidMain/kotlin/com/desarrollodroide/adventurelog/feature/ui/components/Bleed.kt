package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

/**
 * Lets a horizontally scrolling row run out over the page margin it sits in ([bleed] on each side),
 * so what does not fit slides off the screen's edge instead of being sliced at the margin. Give the
 * row the same [bleed] as content padding so its first item still lines up with the page.
 */
fun Modifier.bleedHorizontally(bleed: Dp): Modifier = layout { measurable, constraints ->
    val extra = bleed.roundToPx()
    val wide = if (constraints.hasBoundedWidth) constraints.maxWidth + 2 * extra else constraints.maxWidth
    val placeable = measurable.measure(constraints.copy(minWidth = constraints.minWidth, maxWidth = wide))
    layout((placeable.width - 2 * extra).coerceAtLeast(0), placeable.height) { placeable.place(-extra, 0) }
}
