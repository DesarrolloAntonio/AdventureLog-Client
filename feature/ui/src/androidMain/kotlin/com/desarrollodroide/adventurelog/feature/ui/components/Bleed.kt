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

/**
 * Lets a page reach up [bleed] above the space it was given - over a bar that is fading out above
 * it, say - while still reporting the space it was given, so nothing around it moves.
 */
fun Modifier.bleedUp(bleed: Dp): Modifier = layout { measurable, constraints ->
    val extra = bleed.roundToPx()
    if (extra <= 0 || !constraints.hasBoundedHeight) {
        val placeable = measurable.measure(constraints)
        return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
    val placeable = measurable.measure(
        constraints.copy(minHeight = constraints.minHeight + extra, maxHeight = constraints.maxHeight + extra)
    )
    layout(placeable.width, (placeable.height - extra).coerceAtLeast(0)) { placeable.place(0, -extra) }
}
