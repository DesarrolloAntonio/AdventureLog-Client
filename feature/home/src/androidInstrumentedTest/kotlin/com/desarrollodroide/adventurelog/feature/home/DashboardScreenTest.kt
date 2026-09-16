package com.desarrollodroide.adventurelog.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.desarrollodroide.adventurelog.core.model.Dashboard
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.ui.screen.DashboardScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.ui.test.assertIsDisplayed

/**
 * The dashboard, state by state.
 *
 * These drive the stateless composable directly rather than the view model, so they need no
 * server and no session - which is what makes them worth running on every change.
 */
class DashboardScreenTest {

    private val trip = UltraSlimCollection(
        id = "1",
        name = "Cinque Terre",
        description = "",
        isPublic = false,
        isArchived = false,
        createdAt = "",
        updatedAt = "",
        startDate = "2026-09-05",
        endDate = "2026-09-09",
        adventureCount = 2,
        featuredImage = null,
        link = null,
        status = TripStatus.IN_PROGRESS,
        daysUntilStart = 0
    )

    private val dashboard = Dashboard(
        stats = UserStats(
            locationCount = 22,
            visitedLocationCount = 17,
            tripsCount = 6,
            visitedCountryCount = 6,
            totalCountries = 250,
            visitedRegionCount = 13,
            totalRegions = 5322,
            visitedCityCount = 4,
            totalCities = 153728
        ),
        activeTrip = trip
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun loadingStateShowsNoContent() = runComposeUiTest {
        setContent { WithAppLocals { DashboardScreen(homeUiState = HomeUiState.Loading) } }
        onAllNodes(hasText("Cinque Terre")).assertCountEquals(0)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun errorStateOffersARetry() = runComposeUiTest {
        var retries = 0
        setContent {
            WithAppLocals {
            DashboardScreen(
                homeUiState = HomeUiState.Error("Could not load your dashboard."),
                onRetry = { retries++ }
            )
            }
        }

        onNodeWithText("Could not load your dashboard.").assertIsDisplayed()
        onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun theFeaturedTripLeadsTheScreen() = runComposeUiTest {
        setContent {
            WithAppLocals {
                DashboardScreen(homeUiState = HomeUiState.Success(dashboard = dashboard))
            }
        }

        onNodeWithText("Cinque Terre").assertIsDisplayed()
        onNodeWithText("HAPPENING NOW").assertIsDisplayed()
        onNodeWithText("2 places · 05/09 – 09/09").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun openingTheTripReportsTheTrip() = runComposeUiTest {
        var opened: UltraSlimCollection? = null
        setContent {
            WithAppLocals {
            DashboardScreen(
                homeUiState = HomeUiState.Success(dashboard = dashboard),
                onTripClick = { opened = it }
            )
            }
        }

        onNodeWithText("Open trip").performClick()
        assertEquals("Cinque Terre", opened?.name)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun addingAPlaceFromTheHeroIsWired() = runComposeUiTest {
        // This one exists because the button did nothing on a tablet for a whole release: the
        // wide layout never passed the callback through.
        var added = 0
        setContent {
            WithAppLocals {
            DashboardScreen(
                homeUiState = HomeUiState.Success(dashboard = dashboard),
                onAddPlace = { added++ }
            )
            }
        }

        onNodeWithText("Add a place").performClick()
        assertEquals(1, added)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun statsShowVisitedOverTotal() = runComposeUiTest {
        setContent {
            WithAppLocals {
                DashboardScreen(homeUiState = HomeUiState.Success(dashboard = dashboard))
            }
        }

        // assertExists, not assertIsDisplayed: the stats sit below the fold on a phone, and
        // whether they have been scrolled to is not what this test is about.
        onNodeWithText("Countries").assertExists()
        onNodeWithText("Regions").assertExists()
        onNodeWithText(" / 250").assertExists()
        onNodeWithText(" / 5,322").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun anEmptyAccountGetsAWayIn() = runComposeUiTest {
        var addedPlace = 0
        var addedCollection = 0
        setContent {
            WithAppLocals {
            DashboardScreen(
                homeUiState = HomeUiState.Success(dashboard = Dashboard()),
                onAddPlace = { addedPlace++ },
                onAddCollection = { addedCollection++ }
            )
            }
        }

        // A brand new account should not open on a card of zeros above half a blank page.
        onAllNodes(hasText("Countries")).assertCountEquals(0)
        assertTrue(onAllNodes(hasText("Add a place")).fetchSemanticsNodes().isNotEmpty())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aPendingInvitationIsShownEvenToAnAccountWithNothingYet() = runComposeUiTest {
        // QA 09, MC-05: a brand-new account with an invitation waiting saw only the first-run
        // screen - the dashboard carried the count and nothing showed it.
        var opened = 0
        setContent {
            WithAppLocals {
                DashboardScreen(
                    homeUiState = HomeUiState.Success(dashboard = Dashboard(inviteCount = 1)),
                    onSeeInvitations = { opened++ }
                )
            }
        }

        onNodeWithText("You have an invitation to a collection").assertIsDisplayed().performClick()
        assertEquals(1, opened)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun noInvitationNoBanner() = runComposeUiTest {
        setContent { WithAppLocals { DashboardScreen(homeUiState = HomeUiState.Success(dashboard = dashboard)) } }

        onAllNodes(hasText("invitation", substring = true)).assertCountEquals(0)
    }

}
