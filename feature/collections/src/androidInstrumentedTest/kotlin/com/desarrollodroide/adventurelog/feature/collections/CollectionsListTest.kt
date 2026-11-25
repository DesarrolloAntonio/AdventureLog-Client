package com.desarrollodroide.adventurelog.feature.collections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import coil3.ImageLoader
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionsTab
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionsListTabs
import com.desarrollodroide.adventurelog.feature.collections.ui.components.SlimCollectionItem
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.junit.Assert.assertEquals
import org.junit.Test

/** The Collections list as the canvas draws it: connected tabs, and each trip a ticket. */
@OptIn(ExperimentalTestApi::class)
class CollectionsListTest {

    @Composable
    private fun WithImages(content: @Composable () -> Unit) {
        val context = LocalContext.current
        CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) { content() }
    }

    private val cinqueTerre = UltraSlimCollection(
        id = "1", name = "Cinque Terre", description = "", isPublic = false, isArchived = false,
        createdAt = "", updatedAt = "", startDate = "2026-09-05", endDate = "2026-09-09",
        adventureCount = 2, featuredImage = null, link = null, status = TripStatus.UPCOMING, daysUntilStart = 8,
    )

    @Test
    fun theTicketPrintsNameWhenDatesAndPlaces() = runComposeUiTest {
        setContent { WithImages { SlimCollectionItem(collection = cinqueTerre, onClick = {}) } }

        onNodeWithText("Cinque Terre").assertIsDisplayed()
        onNodeWithText("IN 8 DAYS", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("5–9 SEP", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("PLACES", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun aFolderHasNoDatesToPrint() = runComposeUiTest {
        val folder = cinqueTerre.copy(status = TripStatus.FOLDER, startDate = null, endDate = null, daysUntilStart = null)
        setContent { WithImages { SlimCollectionItem(collection = folder, onClick = {}) } }

        onNodeWithText("DATES", useUnmergedTree = true).assertDoesNotExist()
        onNodeWithText("PLACES", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun tappingOpensAndTheMenuOrALongPressOffersTheActions() = runComposeUiTest {
        var opened = 0
        setContent { WithImages { SlimCollectionItem(collection = cinqueTerre, onClick = { opened++ }) } }

        onNodeWithText("Cinque Terre").performClick()
        assertEquals(1, opened)

        onNodeWithText("Cinque Terre").performTouchInput { longClick() }
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Edit collection").fetchSemanticsNodes().isNotEmpty() }
        onAllNodesWithText("Edit collection").onFirst().assertExists()
    }

    @Test
    fun theTabsAreFourWithInvitesCounted() = runComposeUiTest {
        var chosen: CollectionsTab? = null
        setContent { CollectionsListTabs(selected = CollectionsTab.MINE, pendingInvites = 1, onSelect = { chosen = it }) }

        onNodeWithText("Mine", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Shared", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Archived", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithContentDescription("Invites, 1 waiting").assertIsDisplayed()
        onNodeWithContentDescription("Mine").assertIsSelected()

        onNodeWithContentDescription("Invites, 1 waiting").performClick()
        assertEquals(CollectionsTab.INVITES, chosen)
    }
}
