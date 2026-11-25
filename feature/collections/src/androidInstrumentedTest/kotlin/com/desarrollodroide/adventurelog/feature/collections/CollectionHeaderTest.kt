package com.desarrollodroide.adventurelog.feature.collections

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.ChecklistItem
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.feature.collections.ui.components.AddToCollectionMenu
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionHeader
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/** The top of a collection as the canvas draws it, and the "+" that adds any of its five kinds. */
@OptIn(ExperimentalTestApi::class)
class CollectionHeaderTest {

    private fun item(name: String, checked: Boolean) = ChecklistItem(
        id = name, name = name, isChecked = checked, checklist = "c1", user = "u", createdAt = "", updatedAt = "",
    )

    private val packing = Checklist(
        id = "c1", user = "u", name = "Packing list", date = null, isPublic = false, collection = "1",
        createdAt = "", updatedAt = "",
        items = listOf(item("Boots", true), item("Rain jacket", true), item("Map", true), item("Snacks", false)),
    )

    private val cinqueTerre = Collection(
        id = "1", description = "Five villages", userId = "u", name = "Cinque Terre", isPublic = false,
        locations = emptyList(), createdAt = "", startDate = "2026-09-05", endDate = "2026-09-09",
        transportations = emptyList(), notes = emptyList(), updatedAt = "", checklists = listOf(packing),
        isArchived = false, sharedWith = emptyList(), link = "", lodging = emptyList(),
    )

    @Test
    fun theHeaderCountsDownAndShowsThePackingList() = runComposeUiTest {
        setContent { CollectionHeader(cinqueTerre, showTitle = true, today = LocalDate(2026, 8, 28)) }

        onNodeWithText("PLANNED").assertIsDisplayed()
        onNodeWithText("Cinque Terre").assertIsDisplayed()
        onNodeWithText("8", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("DAYS TO GO", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("5 SEP → 9 SEP", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Packing list · 3 of 4", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun backAndEditAreThere() = runComposeUiTest {
        var back = 0
        var edit = 0
        setContent {
            CollectionHeader(
                cinqueTerre, showTitle = true, today = LocalDate(2026, 8, 28),
                onBackClick = { back++ }, onEditCollection = { edit++ },
            )
        }

        onNodeWithContentDescription("Back").performClick()
        onNodeWithContentDescription("Edit collection").performClick()
        assertEquals(listOf(1, 1), listOf(back, edit))
    }

    @Test
    fun theAddMenuOffersAllFiveKinds() = runComposeUiTest {
        val added = mutableListOf<String>()
        setContent {
            AddToCollectionMenu(
                onAddPlace = { added += "place" },
                onAddTransportation = { added += "transport" },
                onAddLodging = { added += "lodging" },
                onAddNote = { added += "note" },
                onAddChecklist = { added += "checklist" },
            )
        }

        onNodeWithContentDescription("Add to collection").performClick()
        onNodeWithText("Lodging").performClick()
        assertEquals(listOf("lodging"), added)
    }
}
