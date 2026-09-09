package com.desarrollodroide.adventurelog.feature.world

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.feature.world.ui.components.CountryCard
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The country row.
 *
 * Its name used to be white text over the flag, which disappeared on the white and yellow half of
 * the world's flags. These assert the name and the count are their own nodes.
 */
class CountryCardTest {

    private val spain = Country(
        id = 1,
        name = "Spain",
        countryCode = "ES",
        flagUrl = "",
        numRegions = 19,
        numVisits = 4,
        subregion = "Southern Europe",
        capital = "Madrid",
        longitude = null,
        latitude = null
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aCountryShowsItsNameWhereItIsAndHowFarAlong() = runComposeUiTest {
        setContent { CountryCard(country = spain, onClick = {}) }

        onNodeWithText("Spain").assertIsDisplayed()
        onNodeWithText("Southern Europe · Madrid").assertIsDisplayed()
        onNodeWithText("4/19").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun anUnvisitedCountryIsNotShouted() = runComposeUiTest {
        setContent { CountryCard(country = spain.copy(numVisits = 0), onClick = {}) }
        // 244 of the 250 are unvisited; a dash, not a red badge on every row.
        onNodeWithText("–").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingACountryReportsIt() = runComposeUiTest {
        var taps = 0
        setContent { CountryCard(country = spain, onClick = { taps++ }) }
        onNodeWithText("Spain").performClick()
        assertEquals(1, taps)
    }
}
