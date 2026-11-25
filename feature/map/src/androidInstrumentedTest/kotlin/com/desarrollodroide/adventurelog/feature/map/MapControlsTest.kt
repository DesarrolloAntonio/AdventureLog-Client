package com.desarrollodroide.adventurelog.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import coil3.ImageLoader
import com.desarrollodroide.adventurelog.core.testing.testLocation
import com.desarrollodroide.adventurelog.feature.map.ui.components.MapPlaceCard
import com.desarrollodroide.adventurelog.feature.map.ui.components.MapVisibility
import com.desarrollodroide.adventurelog.feature.map.ui.components.MapVisibilityGroup
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.junit.Assert.assertEquals
import org.junit.Test

/** The controls floating over the map, and the card a tapped pin opens. */
@OptIn(ExperimentalTestApi::class)
class MapControlsTest {

    @Composable
    private fun WithImages(content: @Composable () -> Unit) {
        val context = LocalContext.current
        CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) { content() }
    }

    @Test
    fun theGroupCountsAndChooses() = runComposeUiTest {
        var chosen: MapVisibility? = null
        setContent {
            MapVisibilityGroup(visitedCount = 10, plannedCount = 12, selected = MapVisibility.ALL, onSelect = { chosen = it })
        }

        onNodeWithText("All 22", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Visited 10", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithContentDescription("All, 22 places").assertIsOn()

        onNodeWithText("Planned 12", useUnmergedTree = true).performClick()
        assertEquals(MapVisibility.PLANNED, chosen)
    }

    @Test
    fun aTappedPinShowsItsCardWhichOpensThePlace() = runComposeUiTest {
        var opened = 0
        var directions = 0
        val place = testLocation("Geirangerfjord", region = "Møre og Romsdal", category = "Nature", isVisited = true)
            .copy(rating = 5.0)
        setContent {
            WithImages {
                MapPlaceCard(place, onOpen = { opened++ }, onDirections = { directions++ }, onDismiss = {})
            }
        }

        onNodeWithText("Geirangerfjord").assertIsDisplayed()
        onNodeWithText("Møre og Romsdal · Nature", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Open").performClick()
        onNodeWithText("Directions").performClick()
        assertEquals(listOf(1, 1), listOf(opened, directions))
    }
}
