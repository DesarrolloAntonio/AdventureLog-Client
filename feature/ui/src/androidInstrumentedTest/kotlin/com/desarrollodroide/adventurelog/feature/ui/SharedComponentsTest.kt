package com.desarrollodroide.adventurelog.feature.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Title
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import coil3.ImageLoader
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.desarrollodroide.adventurelog.feature.ui.components.PullableStateBox
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.onRoot
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * The pieces that appear on more than one screen, so a regression here is a regression everywhere.
 *
 * Every case below is something that was actually broken during the redesign, not a hypothetical.
 */
class SharedComponentsTest {

    @Composable
    private fun WithAppLocals(content: @Composable () -> Unit) {
        val context = LocalContext.current
        CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) { content() }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aPlaceCardShowsItsNameAndWhereItIs() = runComposeUiTest {
        val place = PreviewData.locations.first()
        setContent { WithAppLocals { AdventureItem(location = place, showMenu = false) } }

        // The name sits over the photograph, on a scrim. It has moved out and back again once
        // already - the legibility problem is real and the scrim is what answers it - so this
        // holds the one thing that must be true either way: the name is on the card.
        onNodeWithText(place.name).assertIsDisplayed()
        place.location?.let { onNodeWithText(it).assertIsDisplayed() }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingAPlaceCardReportsIt() = runComposeUiTest {
        var taps = 0
        val place = PreviewData.locations.first()
        setContent { WithAppLocals { AdventureItem(location = place, showMenu = false, onClick = { taps++ }) } }

        onNodeWithText(place.name).performClick()
        assertEquals(1, taps)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aMultiLineFieldKeepsWhatYouTypeOnSeparateLines() = runComposeUiTest {
        // StyledTextField forced maxLines = 1 on every field, so a note or a description was one
        // line that scrolled sideways however tall the caller made the box.
        var text by mutableStateOf("")
        setContent {
            StyledTextField(
                value = text,
                onValueChange = { text = it },
                label = "Note",
                icon = Icons.Default.Title,
                singleLine = false,
                minLines = 4
            )
        }

        onNodeWithText("Note").performTextInput("first\nsecond")
        assertTrue(text.contains("\n"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aSectionCardCollapsesAndExpands() = runComposeUiTest {
        setContent {
            var open by remember { mutableStateOf(true) }
            SectionCard(
                title = "The stay",
                icon = Icons.Default.Title,
                expanded = open,
                onExpandedChange = { open = it }
            ) {
                Column { androidx.compose.material3.Text("Inside the card") }
            }
        }

        onNodeWithText("Inside the card").assertIsDisplayed()
        onNodeWithText("The stay").performClick()
        onAllNodes(hasText("Inside the card")).assertCountEquals(0)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
    @Test
    fun anEmptyMessageCanStillBePulledToRefresh() = runComposeUiTest {
        // QA 09: a new account's "No collections yet" could not be pulled, so an invitation that
        // arrived while the app was open never came in.
        var refreshes = 0
        setContent {
            PullToRefreshBox(isRefreshing = false, onRefresh = { refreshes++ }) {
                PullableStateBox { Text("No collections yet") }
            }
        }

        onRoot().performTouchInput { swipeDown(startY = top + 20f, endY = bottom - 20f, durationMillis = 600) }
        waitForIdle()

        assertEquals(1, refreshes)
    }


    // QA CO-19: once filled, a field read "QA123" with nothing saying it was the reservation.
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aFilledFieldStillSaysWhatItIs() = runComposeUiTest {
        setContent {
            StyledTextField(value = "QA123", onValueChange = {}, label = "Reservation", icon = Icons.Default.Title)
        }

        onNode(hasSetTextAction() and hasText("Reservation")).assertExists()
        onNode(hasSetTextAction() and hasText("QA123")).assertExists()
    }

}
