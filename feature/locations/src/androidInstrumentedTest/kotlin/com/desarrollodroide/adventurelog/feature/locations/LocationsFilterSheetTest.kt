package com.desarrollodroide.adventurelog.feature.locations

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.feature.locations.model.LocationFilters
import com.desarrollodroide.adventurelog.feature.locations.model.VisitedFilter
import com.desarrollodroide.adventurelog.feature.locations.ui.components.LocationsFilterContent
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel.CategoriesState
import org.junit.Test
import org.junit.Assert.assertEquals

/**
 * The filter sheet is where a place list can quietly go wrong: a filter that reads as off while
 * it is on, or a switch that sends the opposite of what it shows, empties the list and looks
 * like a sync problem instead.
 */
@OptIn(ExperimentalTestApi::class)
class LocationsFilterSheetTest {

    private fun noCategories() = CategoriesState.Success(emptyList())

    @Test
    fun anUntouchedSheetSaysNothingIsFiltered() = runComposeUiTest {
        setContent {
            LocationsFilterContent(
                filters = LocationFilters(),
                categoriesState = noCategories(),
                onFiltersChanged = {},
                onApply = {},
                onCancel = {}
            )
        }

        // includeCollections is on by default, and for a while that alone counted as a filter,
        // so the sheet opened claiming "1 active filters" over an untouched form.
        onNodeWithText("No filters applied").assertIsDisplayed()
    }

    @Test
    fun choosingAVisitStatusCountsAsOneFilter() = runComposeUiTest {
        setContent {
            LocationsFilterContent(
                filters = LocationFilters(visitedFilter = VisitedFilter.VISITED),
                categoriesState = noCategories(),
                onFiltersChanged = {},
                onApply = {},
                onCancel = {}
            )
        }

        onNodeWithText("1 active filter").assertIsDisplayed()
    }

    @Test
    fun turningCollectionsOffIsReportedAsFalse() = runComposeUiTest {
        var latest: LocationFilters? = null
        setContent {
            LocationsFilterContent(
                filters = LocationFilters(),
                categoriesState = noCategories(),
                onFiltersChanged = { latest = it },
                onApply = {},
                onCancel = {}
            )
        }

        onNodeWithText("Collection Locations").performScrollTo()
        // The one switch in the sheet. It shows on, because that is the default, and the point
        // of the test is that what it shows and what it sends are the same thing.
        onNode(isToggleable()).assertIsOn()
        onNode(isToggleable()).performClick()

        assertEquals(false, latest?.includeCollections)
    }

    @Test
    fun pickingAVisitStatusReportsIt() = runComposeUiTest {
        var latest: LocationFilters? = null
        setContent {
            LocationsFilterContent(
                filters = LocationFilters(),
                categoriesState = noCategories(),
                onFiltersChanged = { latest = it },
                onApply = {},
                onCancel = {}
            )
        }

        onNodeWithText("Not Visited").performScrollTo()
        onNodeWithText("Not Visited").performClick()

        assertEquals(VisitedFilter.NOT_VISITED, latest?.visitedFilter)
    }

    @Test
    fun resettingRestoresTheDefaults() = runComposeUiTest {
        var latest: LocationFilters? = null
        setContent {
            LocationsFilterContent(
                filters = LocationFilters(
                    visitedFilter = VisitedFilter.VISITED,
                    includeCollections = false,
                    sortDirection = SortDirection.ASCENDING
                ),
                categoriesState = noCategories(),
                onFiltersChanged = { latest = it },
                onApply = {},
                onCancel = {}
            )
        }

        onNodeWithContentDescription("Reset filters").performClick()

        // Not "everything off": the defaults, which include collections being visible.
        assertEquals(LocationFilters(), latest)
        assertEquals(true, latest?.includeCollections)
    }

    @Test
    fun applyAndCancelAreDistinct() = runComposeUiTest {
        var applied = 0
        var cancelled = 0
        setContent {
            LocationsFilterContent(
                filters = LocationFilters(),
                categoriesState = noCategories(),
                onFiltersChanged = {},
                onApply = { applied++ },
                onCancel = { cancelled++ }
            )
        }

        onNodeWithText("Apply Filters").performClick()
        onNodeWithText("Cancel").performClick()

        assertEquals(1, applied)
        assertEquals(1, cancelled)
    }

    @Test
    fun categoriesThatFailedToLoadOfferARetry() = runComposeUiTest {
        var retries = 0
        setContent {
            LocationsFilterContent(
                filters = LocationFilters(),
                categoriesState = CategoriesState.Error("Could not load categories"),
                onFiltersChanged = {},
                onApply = {},
                onCancel = {},
                onRetryLoadCategories = { retries++ }
            )
        }

        onNodeWithText("Could not load categories").assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
    }
}
