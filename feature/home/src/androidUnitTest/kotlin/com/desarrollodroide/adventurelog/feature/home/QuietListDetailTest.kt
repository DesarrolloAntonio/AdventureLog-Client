package com.desarrollodroide.adventurelog.feature.home

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.ui.screen.withoutAutoFocus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** On a tablet, Places and Collections must not focus their search field - and raise the keyboard - on their own. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class QuietListDetailTest {

    private val twoPanes = PaneScaffoldDirective(
        maxHorizontalPartitions = 2,
        horizontalPartitionSpacerSize = 24.dp,
        maxVerticalPartitions = 1,
        verticalPartitionSpacerSize = 0.dp,
        defaultPanePreferredWidth = 360.dp,
        defaultPanePreferredHeight = 420.dp,
        excludedBounds = listOf(Rect(10f, 0f, 20f, 100f)),
    )

    @Test
    fun theLibraryDefaultMovesFocus() {
        // The reason this file exists: if a release turns it off, the workaround can go.
        assertTrue(twoPanes.shouldAutoFocusCurrentDestination)
    }

    @Test
    fun withoutAutoFocusTurnsItOffAndKeepsTheLayout() {
        val quiet = twoPanes.withoutAutoFocus()

        assertFalse(quiet.shouldAutoFocusCurrentDestination)
        assertEquals(2, quiet.maxHorizontalPartitions)
        assertEquals(24.dp, quiet.horizontalPartitionSpacerSize)
        assertEquals(1, quiet.maxVerticalPartitions)
        assertEquals(0.dp, quiet.verticalPartitionSpacerSize)
        assertEquals(360.dp, quiet.defaultPanePreferredWidth)
        assertEquals(420.dp, quiet.defaultPanePreferredHeight)
        assertEquals(twoPanes.excludedBounds, quiet.excludedBounds)
    }
}
