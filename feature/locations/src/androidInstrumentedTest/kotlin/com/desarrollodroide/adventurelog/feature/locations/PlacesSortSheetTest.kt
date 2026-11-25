package com.desarrollodroide.adventurelog.feature.locations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.feature.locations.model.LocationSortField
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList.PlacesSortSheet
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The sort sheet applies as it is tapped, and a new order starts the way it is read: picking Name
 * out of "newest first" gives A → Z, not Z → A.
 */
@OptIn(ExperimentalTestApi::class)
class PlacesSortSheetTest {

    @Test
    fun pickingNameSortsAToZAndTheDirectionCanBeTurned() = runComposeUiTest {
        var field by mutableStateOf(LocationSortField.UPDATED_AT)
        var direction by mutableStateOf(SortDirection.DESCENDING)
        setContent {
            PlacesSortSheet(
                sortField = field,
                sortDirection = direction,
                onChange = { f, d -> field = f; direction = d },
                onDismiss = {},
            )
        }

        onNodeWithText("Recently updated · Newest first").assertIsDisplayed()

        onNodeWithText("Name").performClick()
        waitForIdle()
        assertEquals(LocationSortField.NAME, field)
        assertEquals(SortDirection.ASCENDING, direction)
        onNodeWithText("Name · A → Z").assertIsDisplayed()

        onNodeWithText("Z → A").performClick()
        waitForIdle()
        assertEquals(SortDirection.DESCENDING, direction)
    }
}
