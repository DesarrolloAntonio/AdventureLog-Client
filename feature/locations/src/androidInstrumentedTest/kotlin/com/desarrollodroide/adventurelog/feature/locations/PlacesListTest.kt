package com.desarrollodroide.adventurelog.feature.locations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.testing.testLocation
import com.desarrollodroide.adventurelog.feature.locations.model.VisitedFilter
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList.VisitedFilterGroup
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList.PlacesView
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList.PlacesViewToggle
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList.rememberPlacesView
import com.desarrollodroide.adventurelog.feature.ui.components.PlaceRow
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Places list as the canvas draws it: compact rows, and All / Visited / Planned. */
@OptIn(ExperimentalTestApi::class)
class PlacesListTest {

    @Composable
    private fun WithImages(content: @Composable () -> Unit) {
        val context = LocalContext.current
        CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) { content() }
    }

    private val trolltunga = testLocation("Trolltunga", region = "Vestland", category = "Hiking")
        .copy(rating = 5.0)

    @Test
    fun aRowSaysWhereWhatAndHowGood() = runComposeUiTest {
        setContent { WithImages { PlaceRow(trolltunga, SegmentedListDefaults.single, onClick = {}) } }

        onNodeWithText("Trolltunga").assertIsDisplayed()
        onNodeWithText("Vestland · Hiking", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithContentDescription("Rated 5 out of 5", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun anUnratedPlaceHasNoRating() = runComposeUiTest {
        setContent { WithImages { PlaceRow(trolltunga.copy(rating = null), SegmentedListDefaults.single, onClick = {}) } }

        onNodeWithContentDescription("Rated", substring = true, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun tappingOpensAndTheMenuOffersTheActions() = runComposeUiTest {
        var opened = 0
        setContent { WithImages { PlaceRow(trolltunga, SegmentedListDefaults.single, onClick = { opened++ }) } }

        onNodeWithText("Trolltunga").performClick()
        assertEquals(1, opened)

        onNodeWithContentDescription("More options").performClick()
        // The sheet is its own window and slides in: wait for it rather than for one frame.
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Edit place").fetchSemanticsNodes().isNotEmpty() }
        onAllNodesWithText("Edit place").onFirst().assertExists()
    }

    @Test
    fun aLongPressOffersTheActionsToo() = runComposeUiTest {
        setContent { WithImages { PlaceRow(trolltunga, SegmentedListDefaults.single, onClick = {}) } }

        onNodeWithText("Trolltunga").performTouchInput { longClick() }
        // The sheet is its own window and slides in: wait for it rather than for one frame.
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Edit place").fetchSemanticsNodes().isNotEmpty() }
        onAllNodesWithText("Edit place").onFirst().assertExists()
    }

    @Test
    fun aLongNameStaysOnOneLine() = runComposeUiTest {
        val long = trolltunga.copy(name = "A place whose name somebody typed out in full, every word of it")
        setContent { WithImages { Box(Modifier.width(360.dp)) { PlaceRow(long, SegmentedListDefaults.single, onClick = {}) } } }

        val layouts = mutableListOf<TextLayoutResult>()
        onNodeWithText(long.name, useUnmergedTree = true).fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        assertEquals(1, layouts.first().lineCount)
    }

    @Test
    fun allVisitedPlannedCountTheWholeLibrary() = runComposeUiTest {
        var chosen: VisitedFilter? = null
        setContent {
            VisitedFilterGroup(
                counts = UserStats(locationCount = 22, visitedLocationCount = 17),
                selected = VisitedFilter.ALL,
                onSelect = { chosen = it },
            )
        }

        onNodeWithText("All 22", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Visited 17", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Planned 5", useUnmergedTree = true).assertIsDisplayed()

        onNodeWithText("Planned 5", useUnmergedTree = true).performClick()
        assertEquals(VisitedFilter.NOT_VISITED, chosen)
    }

    @Test
    fun theChosenFilterIsTheCheckedOne() = runComposeUiTest {
        setContent {
            VisitedFilterGroup(
                counts = UserStats(locationCount = 22, visitedLocationCount = 17),
                selected = VisitedFilter.VISITED,
                onSelect = {},
            )
        }

        onNodeWithContentDescription("Visited, 17 places").assertIsOn()
        onNodeWithContentDescription("All, 22 places").assertIsOff()
    }

    @Test
    fun withoutCountsTheFiltersStillWork() = runComposeUiTest {
        var chosen: VisitedFilter? = null
        setContent { VisitedFilterGroup(counts = null, selected = VisitedFilter.ALL, onSelect = { chosen = it }) }

        onNodeWithText("Visited", useUnmergedTree = true).performClick()
        assertTrue(chosen == VisitedFilter.VISITED)
    }

    @Test
    fun theViewToggleSwitchesBetweenRowsAndPhotos() = runComposeUiTest {
        var chosen: PlacesView? = null
        setContent { PlacesViewToggle(PlacesView.ROWS, onChange = { chosen = it }) }

        onNodeWithContentDescription("Show photos").performClick()
        assertEquals(PlacesView.PHOTOS, chosen)
    }

    @Test
    fun theChosenViewIsRememberedNextTime() = runComposeUiTest {
        var shown by mutableStateOf(true)
        setContent {
            WithImages {
                if (shown) {
                    val (view, onChange) = rememberPlacesView()
                    PlacesViewToggle(view, onChange)
                }
            }
        }
        // Start from the rows, whatever an earlier run left.
        if (onAllNodesWithContentDescription("Show as a list").fetchSemanticsNodes().isNotEmpty()) {
            onNodeWithContentDescription("Show as a list").performClick()
        }
        onNodeWithContentDescription("Show photos").performClick()
        onNodeWithContentDescription("Show as a list").assertIsDisplayed()

        // Gone and back, as on leaving Places and coming back to it.
        shown = false
        waitForIdle()
        shown = true
        onNodeWithContentDescription("Show as a list").assertIsDisplayed()

        onNodeWithContentDescription("Show as a list").performClick()
    }
}
