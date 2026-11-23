package com.desarrollodroide.adventurelog.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.ui.components.MainToolbar
import com.desarrollodroide.adventurelog.feature.home.ui.components.MainToolbarDefaults
import com.desarrollodroide.adventurelog.feature.home.ui.components.MainToolbarRow
import com.desarrollodroide.adventurelog.feature.home.ui.components.rememberToolbarScrollState
import com.desarrollodroide.adventurelog.feature.ui.components.ShellAction
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
    fun aScreenOutsideTheFiveShowsEveryDestinationAsAnIcon() = runComposeUiTest {
        // Settings, Calendar and People live in the shell but are none of the five.
        setContent {
            MainToolbarRow(
                current = CurrentScreen.SETTINGS,
                onSelect = {},
                action = null,
                scroll = rememberToolbarScrollState(),
            )
        }

        listOf("Home", "Places", "Collections", "Map", "World").forEach {
            onNodeWithContentDescription(it).assertIsDisplayed()
        }
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

    /** A list under the floating row, wired the way the shell wires it. */
    @Composable
    private fun ListUnderToolbar(action: ShellAction? = null) {
        val scroll = rememberToolbarScrollState()
        Box(Modifier.fillMaxSize().nestedScroll(scroll.connection).testTag("root")) {
            LazyColumn(Modifier.fillMaxSize().testTag("list")) {
                items(50) { i -> Text("Row $i", Modifier.fillMaxWidth().height(56.dp).testTag("row$i")) }
            }
            MainToolbarRow(
                current = CurrentScreen.PLACES,
                onSelect = {},
                action = action,
                scroll = scroll,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    @Test
    fun contentRunsUnderTheToolbar() = runComposeUiTest {
        setContent { ListUnderToolbar() }

        // Floating means the list goes on behind it: a row is drawn where the toolbar is.
        val toolbar = onNodeWithTag(MainToolbarDefaults.TestTag).getUnclippedBoundsInRoot()
        val root = onNodeWithTag("root").getUnclippedBoundsInRoot()
        val rowsBelowToolbarTop = (0 until 50).count { i ->
            runCatching { onNodeWithTag("row$i").getUnclippedBoundsInRoot() }.getOrNull()
                ?.let { it.bottom > toolbar.top && it.top < root.bottom } == true
        }
        assertTrue("no row behind the toolbar", rowsBelowToolbarTop > 0)
    }

    @Test
    fun scrollingDownHidesTheToolbarAndScrollingUpBringsItBack() = runComposeUiTest {
        setContent { ListUnderToolbar() }
        val rootBottom = onNodeWithTag("root").getUnclippedBoundsInRoot().bottom

        onNodeWithTag("list").performTouchInput { swipeUp() }
        waitForIdle()
        assertTrue(
            "toolbar still on screen after scrolling down",
            onNodeWithTag(MainToolbarDefaults.TestTag).getUnclippedBoundsInRoot().top >= rootBottom,
        )

        onNodeWithTag("list").performTouchInput { swipeDown() }
        waitForIdle()
        assertTrue(
            "toolbar not back after scrolling up",
            onNodeWithTag(MainToolbarDefaults.TestTag).getUnclippedBoundsInRoot().bottom <= rootBottom,
        )
    }

    @Test
    fun theScreensMainActionSitsBesideTheToolbar() = runComposeUiTest {
        var added = false
        setContent { ListUnderToolbar(ShellAction(Icons.Default.Add, "Add a place") { added = true }) }

        val toolbar = onNodeWithTag(MainToolbarDefaults.TestTag).getUnclippedBoundsInRoot()
        val action = onNodeWithContentDescription("Add a place").getUnclippedBoundsInRoot()
        assertTrue(action.left >= toolbar.right)

        onNodeWithContentDescription("Add a place").performClick()
        assertTrue(added)
    }

    @Test
    fun toolbarAndActionFitAPhone() = runComposeUiTest {
        setContent {
            Box(Modifier.width(412.dp).fillMaxSize().testTag("phone")) {
                MainToolbarRow(
                    current = CurrentScreen.COLLECTIONS,
                    onSelect = {},
                    action = ShellAction(Icons.Default.Add, "Add collection") {},
                    scroll = rememberToolbarScrollState(),
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }

        val phone = onNodeWithTag("phone").getUnclippedBoundsInRoot()
        val toolbar = onNodeWithTag(MainToolbarDefaults.TestTag).getUnclippedBoundsInRoot()
        val action = onNodeWithContentDescription("Add collection").getUnclippedBoundsInRoot()
        val margin = MainToolbarDefaults.SideMargin
        assertTrue("toolbar from ${toolbar.left} on a phone from ${phone.left}", toolbar.left >= phone.left + margin)
        assertTrue("action to ${action.right} on a phone to ${phone.right}", action.right <= phone.right - margin)
        // No room for "Collections" beside the button: the pill keeps the icon and says its name.
        onNodeWithText("Collections").assertDoesNotExist()
        onNodeWithContentDescription("Collections").assertIsDisplayed()
    }

    @Test
    fun aShortNameStaysBesideTheAction() = runComposeUiTest {
        setContent {
            Box(Modifier.width(412.dp).fillMaxSize()) {
                MainToolbarRow(
                    current = CurrentScreen.PLACES,
                    onSelect = {},
                    action = ShellAction(Icons.Default.Add, "Add a place") {},
                    scroll = rememberToolbarScrollState(),
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }

        onNodeWithText("Places").assertIsDisplayed()
    }

    private val addPlace = ShellAction(Icons.Default.Add, "Add a place") {}

    @Test
    fun theActionGrowsInAndTheToolbarMovesOverSmoothly() = runComposeUiTest {
        var action by mutableStateOf<ShellAction?>(null)
        setContent {
            Box(Modifier.width(412.dp).fillMaxSize()) {
                MainToolbarRow(
                    current = CurrentScreen.MAP, onSelect = {}, action = action,
                    scroll = rememberToolbarScrollState(), modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
        val before = onNodeWithTag(MainToolbarDefaults.TestTag).getUnclippedBoundsInRoot().left

        mainClock.autoAdvance = false
        action = addPlace
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeBy(48)
        // Mid-way: the button is there and the toolbar is on its way left, not already there.
        onNodeWithContentDescription("Add a place").assertExists()
        val during = onNodeWithTag(MainToolbarDefaults.TestTag).getUnclippedBoundsInRoot().left

        mainClock.advanceTimeBy(2_000)
        val after = onNodeWithTag(MainToolbarDefaults.TestTag).getUnclippedBoundsInRoot().left
        assertTrue("toolbar left before $before, during $during, after $after", before > during && during > after)
    }

    @Test
    fun theActionShrinksAwayRatherThanVanishing() = runComposeUiTest {
        var action by mutableStateOf<ShellAction?>(addPlace)
        setContent {
            Box(Modifier.width(412.dp).fillMaxSize()) {
                MainToolbarRow(
                    current = CurrentScreen.MAP, onSelect = {}, action = action,
                    scroll = rememberToolbarScrollState(), modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
        onNodeWithContentDescription("Add a place").assertIsDisplayed()

        mainClock.autoAdvance = false
        action = null
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeBy(48)
        onNodeWithContentDescription("Add a place").assertExists()

        mainClock.advanceTimeBy(2_000)
        onNodeWithContentDescription("Add a place").assertDoesNotExist()
    }
}
