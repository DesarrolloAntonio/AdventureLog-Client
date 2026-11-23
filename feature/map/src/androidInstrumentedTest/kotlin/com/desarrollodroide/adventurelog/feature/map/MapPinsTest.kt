package com.desarrollodroide.adventurelog.feature.map

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.feature.map.ui.components.ExpressiveClusterBubble
import com.desarrollodroide.adventurelog.feature.map.ui.components.ExpressivePin
import org.junit.Test

/** The map's pins and clusters as the canvas draws them. */
@OptIn(ExperimentalTestApi::class)
class MapPinsTest {

    @Test
    fun aPinShowsItsCategoryButNotItsName() = runComposeUiTest {
        setContent { ExpressivePin(name = "Trolltunga", emoji = "🥾", visited = true, selected = false) }

        onNodeWithText("🥾").assertIsDisplayed()
        onNodeWithText("Trolltunga").assertDoesNotExist()
    }

    @Test
    fun theSelectedPinIsALabelWithItsName() = runComposeUiTest {
        setContent { ExpressivePin(name = "Geirangerfjord", emoji = null, visited = false, selected = true) }

        onNodeWithText("Geirangerfjord").assertIsDisplayed()
    }

    @Test
    fun aClusterSaysHowManyPlaces() = runComposeUiTest {
        setContent { ExpressiveClusterBubble(count = 4) }

        onNodeWithText("4").assertIsDisplayed()
    }
}
