package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The widest a screen's content is allowed to get.
 *
 * Past roughly this width a page stops being one thing and becomes a row of unrelated bands: a
 * search field a metre long, a progress bar too wide to read as a proportion, a card stretched
 * until its title floats alone in the middle of it.
 */
val MaxContentWidth = 1040.dp

/**
 * One content column, centred, for a whole screen.
 *
 * The point is that there is exactly *one* width on the screen. Everything inside shares the same
 * left and right edge - the header, the search field, the filters, the cards - and the extra room
 * on a large window becomes margin rather than three different measurements fighting each other.
 * Capping individual elements instead is what produces that: it is the same mistake repeated at
 * three different numbers.
 *
 * Below the cap this does nothing at all, so phones and foldables are untouched.
 */
@Composable
fun ContentColumn(
    modifier: Modifier = Modifier,
    maxWidth: Dp = MaxContentWidth,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.widthIn(max = maxWidth).fillMaxSize()) {
            content()
        }
    }
}

/**
 * The dashboard is wider than the rest because it carries a second column: a main column and a
 * 348dp data rail, which is what the leftover width on a tablet is actually for.
 */
val MaxDashboardWidth = 1240.dp

/** The data rail's width, from the redesign: `minmax(0,1fr) 348px`. */
val DataRailWidth = 348.dp
