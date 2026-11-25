package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * A place on the map as the canvas draws it: a rounded square on a short point, inked in the
 * primary colour once visited and outlined while only planned, with the category's emoji inside.
 * The one tapped grows into a dark label with its name.
 */
@Composable
internal fun ExpressivePin(
    name: String,
    emoji: String?,
    visited: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    if (selected) {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                Modifier
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.inverseSurface)
                    .height(44.dp)
                    .padding(start = 7.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) { PinGlyph(emoji, colors.onPrimaryContainer, 16) }
                Text(
                    text = name,
                    style = AdventureType.display(18.sp),
                    color = colors.inverseOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PinPoint(colors.inverseSurface)
        }
        return
    }
    val fill = if (visited) colors.primary else colors.surfaceContainerLowest
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .shadow(4.dp, RoundedCornerShape(14.dp))
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(fill)
                .then(if (visited) Modifier else Modifier.border(2.dp, colors.primary, RoundedCornerShape(14.dp))),
            contentAlignment = Alignment.Center,
        ) { PinGlyph(emoji, if (visited) colors.onPrimary else colors.primary, 18) }
        PinPoint(if (visited) fill else colors.primary)
    }
}

@Composable
private fun PinGlyph(emoji: String?, tint: Color, sizeSp: Int) {
    if (!emoji.isNullOrBlank()) {
        Text(emoji, fontSize = sizeSp.sp)
    } else {
        Icon(Icons.Default.Place, contentDescription = null, tint = tint, modifier = Modifier.size((sizeSp + 4).dp))
    }
}

/** The short point under a pin, where it touches the map. */
@Composable
private fun PinPoint(color: Color) {
    Box(
        Modifier
            .offset(y = (-6).dp)
            .size(10.dp)
            .rotate(45f)
            .background(color)
    )
}

/**
 * How many places are stacked at this point: the count in the condensed display face, inside one
 * of Material's expressive shapes ringed in white so it stands off any map. Grows with the count.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ExpressiveClusterBubble(count: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val size = when {
        count >= 100 -> 64.dp
        count >= 25 -> 56.dp
        else -> 50.dp
    }
    val shape = MaterialShapes.Cookie9Sided.toShape()
    Box(
        modifier
            .size(size + 6.dp)
            .clip(shape)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size)
                .clip(shape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = count.toString(),
                style = AdventureType.display(if (count >= 100) 20.sp else 24.sp),
                color = colors.onPrimaryContainer,
            )
        }
    }
}
