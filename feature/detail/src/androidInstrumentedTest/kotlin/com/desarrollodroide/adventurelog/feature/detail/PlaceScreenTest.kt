package com.desarrollodroide.adventurelog.feature.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import coil3.ImageLoader
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.testing.testLocation
import com.desarrollodroide.adventurelog.core.testing.testVisit
import com.desarrollodroide.adventurelog.feature.detail.ui.components.PlaceActionsToolbar
import com.desarrollodroide.adventurelog.feature.detail.ui.screen.AdventureDetailScreen
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.junit.Assert.assertEquals
import org.junit.Test

/** A place's page as the canvas draws it: chips, name, where, stars, the facts strip, the toolbar. */
@OptIn(ExperimentalTestApi::class)
class PlaceScreenTest {

    @Composable
    private fun WithImages(content: @Composable () -> Unit) {
        val context = LocalContext.current
        CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) { content() }
    }

    private val fjords = UltraSlimCollection(
        id = "c1", name = "Fjords", description = "", isPublic = false, isArchived = false,
        createdAt = "", updatedAt = "", startDate = "2026-07-03", endDate = "2026-07-11",
        adventureCount = 3, featuredImage = null, link = null, status = TripStatus.COMPLETED,
    )

    private val trolltunga = testLocation(
        "Trolltunga",
        isVisited = true,
        category = "Hiking",
        region = "Vestland",
        country = "Norway",
        // No coordinates: the page's map is a Google Maps view, which crashes on teardown in a
        // test host (AndroidViewHolder detached). The toolbar's maps button is tested on its own.
        visits = listOf(testVisit("v1", "2026-07-06")),
    ).copy(rating = 5.0, link = "https://example.org/trolltunga")

    @Test
    fun theHeaderSaysWhatWhereAndHowGood() = runComposeUiTest {
        setContent {
            WithImages {
                AdventureDetailScreen(location = trolltunga, onBackClick = {}, onOpenMap = { _, _ -> }, onOpenLink = {})
            }
        }

        onNodeWithText("Trolltunga").assertIsDisplayed()
        onNodeWithText("Hiking").assertIsDisplayed()
        onNodeWithText("Visited").assertIsDisplayed()
        onNodeWithText("Vestland, Norway").assertIsDisplayed()
        onNodeWithContentDescription("Rated 5 out of 5").assertIsDisplayed()
    }

    @Test
    fun theFactsStripOpensTheTrip() = runComposeUiTest {
        var opened: UltraSlimCollection? = null
        setContent {
            WithImages {
                AdventureDetailScreen(
                    location = trolltunga,
                    collections = listOf(fjords),
                    onBackClick = {},
                    onOpenMap = { _, _ -> },
                    onOpenLink = {},
                    onCollectionClick = { opened = it },
                )
            }
        }

        onNodeWithText("6 JUL 26", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        onNodeWithContentDescription("Trip: Fjords").performScrollTo().performClick()
        assertEquals("Fjords", opened?.name)
    }

    @Test
    fun theToolbarOpensMapsTheWebsiteAndTheEditor() = runComposeUiTest {
        var maps = 0
        var sites = 0
        var edits = 0
        setContent {
            PlaceActionsToolbar(onOpenMaps = { maps++ }, onOpenWebsite = { sites++ }, onEdit = { edits++ })
        }

        onNodeWithContentDescription("Open in maps").performClick()
        onNodeWithContentDescription("Website").performClick()
        onNodeWithContentDescription("Edit place").performClick()
        assertEquals(listOf(1, 1, 1), listOf(maps, sites, edits))
    }

    @Test
    fun thePageWiresTheWebsiteAndTheEditor() = runComposeUiTest {
        var link: String? = null
        var edits = 0
        setContent {
            WithImages {
                AdventureDetailScreen(
                    location = trolltunga,
                    onBackClick = {},
                    onEditClick = { edits++ },
                    onOpenMap = { _, _ -> },
                    onOpenLink = { link = it },
                )
            }
        }

        onNodeWithContentDescription("Website").performClick()
        onNodeWithContentDescription("Edit place").performClick()
        assertEquals("https://example.org/trolltunga", link)
        assertEquals(1, edits)
    }

    @Test
    fun aPlaceWithoutCoordinatesOrLinkOffersNeither() = runComposeUiTest {
        setContent {
            WithImages {
                AdventureDetailScreen(
                    location = trolltunga.copy(link = null),
                    onBackClick = {},
                    onEditClick = {},
                    onOpenMap = { _, _ -> },
                    onOpenLink = {},
                )
            }
        }

        onNodeWithContentDescription("Open in maps").assertDoesNotExist()
        onNodeWithContentDescription("Website").assertDoesNotExist()
        onNodeWithContentDescription("Edit place").assertIsDisplayed()
    }

    @Test
    fun aPlannedPlaceSaysSo() = runComposeUiTest {
        setContent {
            WithImages {
                AdventureDetailScreen(
                    location = trolltunga.copy(isVisited = false, visits = emptyList()),
                    onBackClick = {},
                    onOpenMap = { _, _ -> },
                    onOpenLink = {},
                )
            }
        }

        onNodeWithText("Planned").assertIsDisplayed()
        onNodeWithText("VISITED", useUnmergedTree = true).assertDoesNotExist()
    }
}
