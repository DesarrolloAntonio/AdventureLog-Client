package com.desarrollodroide.adventurelog.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.desarrollodroide.adventurelog.core.model.Dashboard
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.ui.screen.DashboardScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import com.desarrollodroide.adventurelog.core.testing.testLocation
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
                DashboardScreen(homeUiState = HomeUiState.Success(dashboard = dashboard), firstName = "John")
            }
        }

        // The greeting is the headline, the trip a ticket under it with its stubs.
        onNodeWithText("Hi John, you are in Cinque Terre").assertIsDisplayed()
        onNodeWithText("Cinque Terre").assertIsDisplayed()
        onNodeWithText("5 SEP", useUnmergedTree = true).assertExists()
        onNodeWithText("9 SEP", useUnmergedTree = true).assertExists()
        onNodeWithText("PLACES", useUnmergedTree = true).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun withNoTripTheHeadlineSaysWhatHasBeenDone() = runComposeUiTest {
        setContent {
            WithAppLocals {
                DashboardScreen(
                    homeUiState = HomeUiState.Success(dashboard = dashboard.copy(activeTrip = null)),
                    firstName = "John",
                )
            }
        }

        onNodeWithText("Hi John, 17 places visited so far").assertIsDisplayed()
        onAllNodes(hasText("PLACES")).assertCountEquals(0)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun yourMapSoFarOpensTheWorld() = runComposeUiTest {
        var opened = 0
        setContent {
            WithAppLocals {
                DashboardScreen(
                    homeUiState = HomeUiState.Success(dashboard = dashboard),
                    onSeeWorld = { opened++ },
                )
            }
        }

        // Visited countries only: the dashboard has no count of planned ones.
        onNodeWithText("22 places · 6 countries visited").performScrollTo().performClick()
        assertEquals(1, opened)
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

        onNodeWithText("Cinque Terre").performClick()
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

        onNodeWithContentDescription("Add a place").performClick()
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


    // QA HM-09: "Recently updated (22)" sat over three cards - the number was the whole library.
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun recentlyUpdatedDoesNotClaimTheLibraryTotal() = runComposeUiTest {
        setContent {
            WithAppLocals {
                DashboardScreen(
                    homeUiState = HomeUiState.Success(
                        dashboard = dashboard.copy(recentLocations = listOf(testLocation("Machu Picchu"), testLocation("Petra")))
                    )
                )
            }
        }

        onNodeWithText("Recently updated").assertExists()
        onNodeWithText("Recently updated (22)").assertDoesNotExist()
    }

}
