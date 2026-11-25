package com.desarrollodroide.adventurelog.feature.world

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.feature.world.ui.components.CountryRow
import com.desarrollodroide.adventurelog.feature.world.ui.components.PassportCard
import com.desarrollodroide.adventurelog.feature.world.ui.components.StampsStrip
import com.desarrollodroide.adventurelog.feature.world.ui.components.WorldFilterGroup
import com.desarrollodroide.adventurelog.feature.world.ui.state.FilterMode
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import org.junit.Assert.assertEquals
import org.junit.Test

/** World as the canvas draws it: the passport card, the filter group, a country as a stamped row. */
@OptIn(ExperimentalTestApi::class)
class WorldPiecesTest {

    private fun country(code: String, name: String, regions: Int, visited: Int) = Country(
        id = code.hashCode(), name = name, countryCode = code, flagUrl = "", numRegions = regions,
        numVisits = visited, subregion = "Northern Europe", capital = null, longitude = null, latitude = null,
    )

    @Test
    fun thePassportCountsCountriesAndRegions() = runComposeUiTest {
        setContent {
            PassportCard(visitedCountries = 7, totalCountries = 250, regionsVisited = 14, complete = 1, partial = 6)
        }

        onNodeWithText("7", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("countries visited", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("of 250", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("14", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun aCountryRowIsAStampAndItsRegions() = runComposeUiTest {
        var opened: String? = null
        setContent {
            CountryRow(country("NO", "Norway", 15, 3), SegmentedListDefaults.single, onClick = { opened = it })
        }

        onNodeWithText("NO", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Norway", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("3 of 15 regions", substring = true, useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Norway", useUnmergedTree = true).performClick()
        assertEquals("NO", opened)
    }

    @Test
    fun anUnvisitedCountryIsQuiet() = runComposeUiTest {
        // 244 of the 250 are unvisited: an outlined stamp and a plain count, not a badge on every row.
        setContent { CountryRow(country("ES", "Spain", 19, 0), SegmentedListDefaults.single, onClick = {}) }

        onNodeWithText("0 of 19 regions", substring = true, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun theFilterGroupChooses() = runComposeUiTest {
        var chosen: FilterMode? = null
        setContent { WorldFilterGroup(selected = FilterMode.ALL, onSelect = { chosen = it }) }

        onNodeWithContentDescription("All").assertIsOn()
        onNodeWithText("Partial").performClick()
        assertEquals(FilterMode.PARTIAL, chosen)
    }

    @Test
    fun theVisitedCountriesAreStampsThatOpen() = runComposeUiTest {
        var opened: String? = null
        setContent {
            StampsStrip(listOf(country("NO", "Norway", 15, 3), country("JP", "Japan", 47, 3)), onClick = { opened = it })
        }

        onNodeWithText("Your stamps").assertIsDisplayed()
        onNodeWithText("NO", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Japan", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("Japan", useUnmergedTree = true).performClick()
        assertEquals("JP", opened)
    }
}
