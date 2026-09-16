package com.desarrollodroide.adventurelog.feature.collections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import coil3.ImageLoader
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionActionsSheet
import com.desarrollodroide.adventurelog.feature.ui.components.LocationActionsSheet
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.junit.Test

/**
 * What someone a collection is shared with is offered (QA 09, MC-02).
 *
 * Measured with two accounts: the guest was given the owner's whole menu, and the server refused
 * every owner-only entry - a 404 on the collection, a 403 deleting the owner's place - with messages
 * telling them to try again. Each case has its pair, so hiding everything from everyone fails too.
 */
class GuestActionsTest {

    @Composable
    private fun WithAppLocals(content: @Composable () -> Unit) {
        val context = LocalContext.current
        CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) { content() }
    }

    private val trip = UltraSlimCollection(
        id = "c1", name = "Peru", description = "", isPublic = false, isArchived = false,
        createdAt = "", updatedAt = "", startDate = null, endDate = null, adventureCount = 1,
        featuredImage = null, link = null, ownerId = "owner-uuid"
    )

    @Composable
    private fun CollectionSheet(isOwner: Boolean) = CollectionActionsSheet(
        collection = trip, busyLabel = null, isOwner = isOwner,
        onDismiss = {}, onOpen = {}, onEdit = {}, onShare = {}, onShareWithPeople = {}, onArchive = {},
        onDownloadPdf = {}, onExportZip = {}, onDuplicate = {}, onDelete = {}
    )

    @Composable
    private fun PlaceSheet(isOwner: Boolean) = LocationActionsSheet(
        locationName = "Machu Picchu", locationPlace = "Cusco, Peru", isOwner = isOwner,
        onDismiss = {}, onOpenDetails = {}, onEdit = {}, onDuplicate = {}, onShare = {},
        onManageCollections = {}, onDelete = {}
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aGuestIsNotOfferedWhatOnlyTheOwnerOfACollectionMayDo() = runComposeUiTest {
        setContent { WithAppLocals { CollectionSheet(isOwner = false) } }

        onNodeWithText("Open collection").assertIsDisplayed()
        onNodeWithText("Share externally").assertIsDisplayed()
        onNodeWithText("Download PDF").assertIsDisplayed()
        listOf("Edit collection", "Share with people", "Duplicate", "Export ZIP", "Archive", "Delete")
            .forEach { onNodeWithText(it).assertDoesNotExist() }
        onNodeWithText("Shared with you. Only its owner can change, share or delete it.").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun theOwnerOfACollectionStillHasEverything() = runComposeUiTest {
        setContent { WithAppLocals { CollectionSheet(isOwner = true) } }

        listOf("Open collection", "Edit collection", "Share with people", "Share externally", "Duplicate", "Download PDF")
            .forEach { onNodeWithText(it).assertExists() }
        onNodeWithText("Shared with you. Only its owner can change, share or delete it.").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aGuestMayEditTheOwnersPlaceButNotDeleteItOrRefileIt() = runComposeUiTest {
        setContent { WithAppLocals { PlaceSheet(isOwner = false) } }

        onNodeWithText("Edit place").assertIsDisplayed()
        onNodeWithText("Duplicate").assertIsDisplayed()
        onNodeWithText("Manage collections").assertDoesNotExist()
        onNodeWithText("Delete").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun theOwnerOfAPlaceCanStillDeleteAndRefileIt() = runComposeUiTest {
        setContent { WithAppLocals { PlaceSheet(isOwner = true) } }

        onNodeWithText("Manage collections").assertExists()
        onNodeWithText("Delete").assertExists()
    }
}
