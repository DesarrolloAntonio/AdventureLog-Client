package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * What a place with no photo shows in the photo's place (design round 11, "Lugares mezclados").
 *
 * The layout never depends on there being photos: the picture's slot is always there, and when it
 * is empty it is filled on purpose rather than left looking like a load that failed. With a
 * category, a tonal teal surface and the category's emoji. Without one, a neutral surface and a line
 * pin - the fallback never invents an emoji, because categories are the user's own.
 *
 * This replaced a gradient across the theme, which took the same room as a photograph and said
 * nothing, and before that a survey grid under a Static Maps thumbnail that never loaded.
 *
 * The mark is measured from the height it is given: the same component runs at 56dp beside the
 * detail page's "No photo yet", 72dp in the home strip and a whole card in the places list. It is
 * in dp and not sp - a picture should not grow with the reader's font size out of its frame.
 *
 * [bottomInset] is the band along the bottom edge the caller covers with something of its own,
 * such as a card's title. The mark is centred above it instead of behind the writing.
 */
@Composable
fun LocationPlaceholder(
    icon: String?,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp
) {
    val emoji = icon?.takeIf { it.isNotBlank() }
    val colors = placeholderColors(hasCategory = emoji != null)
    BoxWithConstraints(modifier = modifier.background(colors.fill)) {
        val pictureHeight = maxHeight - bottomInset.coerceIn(0.dp, maxHeight)
        // 30 in 72 and 52 in 132, as the design draws them.
        val mark = (pictureHeight * 0.4f).coerceIn(22.dp, 56.dp)
        Box(
            modifier = Modifier.fillMaxWidth().height(pictureHeight),
            contentAlignment = Alignment.Center
        ) {
            if (emoji != null) {
                Text(text = emoji, fontSize = with(LocalDensity.current) { mark.toSp() })
            } else {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = colors.mark,
                    modifier = Modifier.size(mark)
                )
            }
        }
    }
}

/**
 * The two materials a place without a photo is drawn on, as theme roles so dark theme and the
 * wallpaper palette follow.
 *
 * [fill] is the picture's surface, [band] the one step deeper that text laid over it sits on, and
 * [edge] the hairline that keeps the surface from reading as a gap in the card.
 */
@Immutable
data class PlaceholderColors(
    val fill: Color,
    val band: Color,
    val edge: Color,
    val mark: Color
)

@Composable
fun placeholderColors(hasCategory: Boolean): PlaceholderColors {
    val scheme = MaterialTheme.colorScheme
    // Opaque, so a band or a scrim laid over it composes against the same colour on every card.
    val base = scheme.surfaceContainerLowest
    return if (hasCategory) {
        PlaceholderColors(
            fill = scheme.secondaryContainer.copy(alpha = 0.6f).compositeOver(base),
            band = scheme.secondaryContainer.copy(alpha = 0.9f).compositeOver(base),
            edge = scheme.secondaryContainer,
            mark = scheme.onSecondaryContainer
        )
    } else {
        PlaceholderColors(
            fill = scheme.surfaceContainer,
            band = scheme.surfaceContainerHigh,
            edge = scheme.surfaceContainerHighest,
            mark = scheme.outline
        )
    }
}
