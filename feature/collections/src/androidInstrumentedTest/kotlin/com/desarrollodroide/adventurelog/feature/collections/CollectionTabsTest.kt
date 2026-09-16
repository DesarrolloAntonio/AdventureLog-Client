package com.desarrollodroide.adventurelog.feature.collections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import coil3.ImageLoader
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.ChecklistItem
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.Lodging
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionTab
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionDetailContent
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.junit.Assert.assertEquals
import org.junit.Test
import androidx.compose.ui.test.onNodeWithContentDescription

/**
 * The six tabs of a collection.
 *
 * Three of them were greyed out until this week, not for want of data but because the mapper
 * reduced every note, checklist and lodging the server sends to its id. These render the objects,
 * so the day someone reintroduces that shortcut the tabs go empty and this fails.
 */
class CollectionTabsTest {

    @Composable
    private fun WithAppLocals(content: @Composable () -> Unit) {
        val context = LocalContext.current
        CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) { content() }
    }

    private val collection = Collection(
        id = "c1",
        description = "Cusco as a base",
        userId = "u1",
        name = "Peru",
        isPublic = false,
        locations = emptyList(),
        createdAt = "2026-01-01",
        startDate = "2026-10-10",
        endDate = "2026-10-22",
        transportations = emptyList(),
        notes = listOf(
            Note(
                id = "n1",
                user = "u1",
                name = "Machu Picchu tickets",
                content = "Book the 6am entry slot",
                createdAt = "2026-01-01",
                updatedAt = "2026-01-01"
            )
        ),
        updatedAt = "2026-01-01",
        checklists = listOf(
            Checklist(
                id = "k1",
                user = "u1",
                name = "Packing",
                createdAt = "2026-01-01",
                updatedAt = "2026-01-01",
                items = listOf(
                    ChecklistItem("i1", "u1", "Hiking boots", true, "k1", "", ""),
                    ChecklistItem("i2", "u1", "Altitude tablets", false, "k1", "", "")
                )
            )
        ),
        isArchived = false,
        sharedWith = emptyList(),
        link = "",
        lodging = listOf(
            Lodging(
                id = "l1",
                user = "u1",
                name = "Hotel Monasterio",
                type = "hotel",
                checkIn = "2026-10-10",
                checkOut = "2026-10-13",
                location = "Cusco, Peru",
                createdAt = "2026-01-01",
                updatedAt = "2026-01-01"
            )
        )
    )

    @Composable
    private fun Content(tab: CollectionTab, onTab: (CollectionTab) -> Unit = {}) {
        WithAppLocals {
            CollectionDetailContent(
                collection = collection,
                selectedTab = tab,
                onTabSelected = onTab,
                onAdventureClick = {},
                onEditAdventure = {},
                onDeleteAdventure = {},
                onManageCollections = {},
                onAddTransportation = {},
                onEditTransportation = {},
                onDeleteTransportation = {}
            )
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun notesTabListsItsNotes() = runComposeUiTest {
        setContent { Content(CollectionTab.NOTES) }
        onNodeWithText("Machu Picchu tickets").assertIsDisplayed()
        onNodeWithText("Book the 6am entry slot").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun checklistsTabCountsWhatIsDone() = runComposeUiTest {
        setContent { Content(CollectionTab.CHECKLISTS) }
        onNodeWithText("Packing").assertIsDisplayed()
        // One of two ticked; the badge is the only place that says so.
        onNodeWithText("1 / 2").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun lodgingTabShowsTheStayAndItsDates() = runComposeUiTest {
        setContent { Content(CollectionTab.LODGING) }
        onNodeWithText("Hotel Monasterio").assertIsDisplayed()
        onNodeWithText("Cusco, Peru").assertIsDisplayed()
        onNodeWithText("10/10 – 13/10").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun choosingATabReportsIt() = runComposeUiTest {
        var chosen: CollectionTab? = null
        setContent { Content(CollectionTab.ALL) { chosen = it } }
        // onFirst, because the All tab prints "Places" twice: once as the tab, once as the
        // heading over the list.
        onAllNodesWithText("Places").onFirst().performClick()
        assertEquals(CollectionTab.LOCATIONS, chosen)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun everyTabIsReachableWithoutScrolling() = runComposeUiTest {
        var chosen: CollectionTab? = null
        setContent { Content(CollectionTab.ALL) { chosen = it } }

        // The last tab of six. It used to sit off the right edge of a phone behind a sideways
        // scroll nothing announced, and a click on it landed on whatever was underneath.
        onNodeWithText("Checklists").performClick()
        assertEquals(CollectionTab.CHECKLISTS, chosen)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun theBinOnANoteAsksFirstAndOnlyDeletesOnConfirm() = runComposeUiTest {
        // QA 04, CO-07: one tap on the bin deleted the note on the server, no question asked.
        var deleted = 0
        setContent {
            WithAppLocals {
                CollectionDetailContent(
                    collection = collection, selectedTab = CollectionTab.NOTES, onTabSelected = {},
                    onAdventureClick = {}, onEditAdventure = {}, onDeleteAdventure = {}, onManageCollections = {},
                    onAddTransportation = {}, onEditTransportation = {}, onDeleteTransportation = {},
                    onDeleteNote = { deleted++ }
                )
            }
        }

        onNodeWithContentDescription("Delete").performClick()
        onNodeWithText("Delete this note?").assertIsDisplayed()
        assertEquals(0, deleted)

        onNodeWithText("Cancel").performClick()
        onNodeWithText("Delete this note?").assertDoesNotExist()
        assertEquals(0, deleted)

        onNodeWithContentDescription("Delete").performClick()
        onAllNodesWithText("Delete").onFirst().performClick()
        assertEquals(1, deleted)
    }


    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aPlaceInsideACollectionCanBeDuplicatedSharedAndTakenOut() = runComposeUiTest {
        // QA 04: Duplicate and Share externally did nothing here (CO-09), and there was no way
        // to take a place out of just this collection (CO-10).
        val place = com.desarrollodroide.adventurelog.core.model.preview.PreviewData.locations.first()
        val done = mutableListOf<String>()
        setContent {
            WithAppLocals {
                CollectionDetailContent(
                    collection = collection.copy(locations = listOf(place)), selectedTab = CollectionTab.LOCATIONS,
                    onTabSelected = {}, onAdventureClick = {}, onEditAdventure = {}, onDeleteAdventure = {},
                    onManageCollections = {}, onAddTransportation = {}, onEditTransportation = {}, onDeleteTransportation = {},
                    onDuplicateAdventure = { done += "duplicate ${it.id}" },
                    onShareAdventure = { done += "share ${it.id}" },
                    onRemoveFromCollection = { done += "remove ${it.id}" }
                )
            }
        }

        val expected = mutableListOf<String>()
        listOf("Duplicate" to "duplicate", "Share externally" to "share", "Remove from collection" to "remove")
            .forEach { (action, what) ->
                onNodeWithContentDescription("More options").performClick()
                waitUntil(timeoutMillis = 5_000) { onAllNodesWithText(action).fetchSemanticsNodes().isNotEmpty() }
                onNodeWithText(action).performClick()
                // Let the sheet finish leaving before the next one opens: opening it again while it
                // was still animating out crashed inside the sheet's own coroutine, not in the app.
                waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Remove from collection").fetchSemanticsNodes().isEmpty() }
                expected += "$what ${place.id}"
                assertEquals(expected, done)
            }
    }

}
