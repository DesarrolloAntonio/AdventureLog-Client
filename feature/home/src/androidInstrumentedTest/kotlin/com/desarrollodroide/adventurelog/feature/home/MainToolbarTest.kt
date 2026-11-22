package com.desarrollodroide.adventurelog.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.ui.components.MainToolbar
import com.desarrollodroide.adventurelog.feature.home.ui.components.MainToolbarDefaults
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The floating toolbar a phone gets in place of the bottom bar. */
@OptIn(ExperimentalTestApi::class)
class MainToolbarTest {

    @Test
    fun selectedDestinationShowsItsLabelOthersOnlyAnIcon() = runComposeUiTest {
        setContent { MainToolbar(current = CurrentScreen.HOME, onSelect = {}) }

        onNodeWithText("Home").assertIsDisplayed()
        onNodeWithContentDescription("Map").assertIsDisplayed()
        onNodeWithText("Map").assertDoesNotExist()
    }

    @Test
    fun tappingADestinationSelectsIt() = runComposeUiTest {
        var selected: CurrentScreen? = null
        setContent { MainToolbar(current = CurrentScreen.HOME, onSelect = { selected = it }) }

        onNodeWithContentDescription("Map").performClick()

        assertEquals(CurrentScreen.MAP, selected)
    }

    @Test
    fun largeFontKeepsTheLabelAndTheHeight() = runComposeUiTest {
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                MainToolbar(
                    current = CurrentScreen.COLLECTIONS,
                    onSelect = {},
                    modifier = Modifier.testTag("toolbar"),
                )
            }
        }

        onNodeWithText("Collections").assertIsDisplayed()
        onNodeWithTag("toolbar").assertHeightIsEqualTo(MainToolbarDefaults.Height)
    }

    @Test
    fun lastListItemClearsTheToolbar() = runComposeUiTest {
        setContent {
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("list"),
                    contentPadding = PaddingValues(bottom = MainToolbarDefaults.clearance()),
                ) {
                    items(50) { i ->
                        Text("Row $i", Modifier.fillMaxWidth().height(56.dp).testTag("row$i"))
                    }
                }
                MainToolbar(
                    current = CurrentScreen.PLACES,
                    onSelect = {},
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .then(MainToolbarDefaults.PositionModifier)
                        .testTag("toolbar"),
                )
            }
        }

        onNodeWithTag("list").performScrollToIndex(49)

        val row = onNodeWithTag("row49").getUnclippedBoundsInRoot()
        val toolbar = onNodeWithTag("toolbar").getUnclippedBoundsInRoot()
        assertTrue("row ends at ${row.bottom}, toolbar starts at ${toolbar.top}", row.bottom <= toolbar.top)
    }
}
