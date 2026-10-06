package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.ArchiveCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DuplicateCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ExportCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetArchivedCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionInvitesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionsPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetSharedCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.RespondToCollectionInviteUseCase
import com.desarrollodroide.adventurelog.core.model.CollectionInvite
import com.desarrollodroide.adventurelog.core.model.PublicUser
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.testing.CollectionsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.SharingRepositoryStub
import com.desarrollodroide.adventurelog.feature.collections.ui.components.SheetMessage
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import com.desarrollodroide.adventurelog.feature.ui.util.PlatformFiles
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Sharing a collection and being invited to one, measured with three real accounts (QA 09).
 *
 * Two things went wrong there that no one could see from a single account: every message the
 * share sheet raised was drawn behind the sheet, and an invitation that arrived while the app
 * was open could never be reached.
 */
class CollectionsSharingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private class Repo : CollectionsRepositoryStub() {
        var invites: List<CollectionInvite> = emptyList()
        override suspend fun getInvites(): Either<ApiResponse, List<CollectionInvite>> = Either.Right(invites)
        override suspend fun getAllCollections(forceRefresh: Boolean): Either<ApiResponse, List<UltraSlimCollection>> =
            Either.Right(emptyList())
    }

    private class Sharing : SharingRepositoryStub() {
        var answer: CompletableDeferred<Either<String, Unit>> = CompletableDeferred(Either.Right(Unit))
        override suspend fun getPublicUsers(): Either<String, List<PublicUser>> = Either.Right(listOf(guest))
        override suspend fun share(collectionId: String, userUuid: String): Either<String, Unit> = answer.await()
    }

    private object NoFiles : PlatformFiles {
        override suspend fun open(bytes: ByteArray, fileName: String) = throw AssertionError("unused")
        override suspend fun share(bytes: ByteArray, fileName: String) = throw AssertionError("unused")
        override suspend fun delete() = Unit
    }

    private fun viewModel(repo: Repo = Repo(), sharing: Sharing = Sharing()) = CollectionsViewModel(
        getCollectionsPagingUseCase = GetCollectionsPagingUseCase(repo),
        getAllCollectionsUseCase = GetAllCollectionsUseCase(repo),
        deleteCollectionUseCase = DeleteCollectionUseCase(repo),
        observeCollectionsUseCase = ObserveCollectionsUseCase(repo),
        duplicateCollectionUseCase = DuplicateCollectionUseCase(repo),
        archiveCollectionUseCase = ArchiveCollectionUseCase(repo),
        exportCollectionUseCase = ExportCollectionUseCase(repo),
        platformFiles = NoFiles,
        getArchivedCollectionsUseCase = GetArchivedCollectionsUseCase(repo),
        getSharedCollectionsUseCase = GetSharedCollectionsUseCase(repo),
        getCollectionInvitesUseCase = GetCollectionInvitesUseCase(repo),
        respondToCollectionInviteUseCase = RespondToCollectionInviteUseCase(repo),
        sharingRepository = sharing
    )

    @Test
    fun aRefusedInvitationIsSaidInsideTheSheet() = runTest(dispatcher) {
        val sharing = Sharing().apply { answer = CompletableDeferred(Either.Left("Invite already sent to this user")) }
        val vm = viewModel(sharing = sharing)
        vm.openSharing(trip)
        advanceUntilIdle()

        vm.invite(guest)
        advanceUntilIdle()

        // The server's own words, in the sheet - not in a snackbar the sheet covers.
        assertEquals(SheetMessage("Invite already sent to this user", isError = true), vm.sharing.value?.state?.message)
        assertNull(vm.actionMessage.value)
    }

    @Test
    fun aSentInvitationIsSaidInsideTheSheetToo() = runTest(dispatcher) {
        val vm = viewModel()
        vm.openSharing(trip)
        advanceUntilIdle()

        vm.invite(guest)
        advanceUntilIdle()

        assertEquals(SheetMessage("Invitation sent to @claude2", isError = false), vm.sharing.value?.state?.message)
        assertTrue(guest.uuid in vm.sharing.value!!.state.invitedThisVisit)
    }

    @Test
    fun anAnswerThatArrivesAfterTheSheetClosedGoesToTheScreen() = runTest(dispatcher) {
        val sharing = Sharing().apply { answer = CompletableDeferred() }
        val vm = viewModel(sharing = sharing)
        vm.openSharing(trip)
        advanceUntilIdle()

        vm.invite(guest)
        advanceUntilIdle()
        vm.closeSharing()
        sharing.answer.complete(Either.Left("Can't reach the server. Check your connection."))
        advanceUntilIdle()

        // With nothing left to say it in, the snackbar is visible again and takes it.
        assertEquals("Can't reach the server. Check your connection.", vm.actionMessage.value)
    }

    @Test
    fun aRefreshPicksUpAnInvitationThatArrivedWhileTheScreenWasOpen() = runTest(dispatcher) {
        val repo = Repo()
        val vm = viewModel(repo = repo)
        advanceUntilIdle()
        assertEquals(emptyList(), vm.pendingInvites.value)

        repo.invites = listOf(invite)
        vm.refresh()
        advanceUntilIdle()

        // The banner is drawn from this list, and the banner is the only way into Invites.
        assertEquals(listOf(invite), vm.pendingInvites.value)
    }

    @Test
    fun arrivingFromHomeAsksForInvitationsAgainEvenOnTheInvitesTab() = runTest(dispatcher) {
        val repo = Repo()
        val vm = viewModel(repo = repo)
        vm.onTabSelected(com.desarrollodroide.adventurelog.feature.collections.model.CollectionsTab.INVITES)
        advanceUntilIdle()
        assertEquals(emptyList(), vm.tabContent.value.invites)

        repo.invites = listOf(invite)
        vm.showInvites()
        advanceUntilIdle()

        // Home's banner says one is waiting; the tab must not show a list read before it arrived.
        assertEquals(listOf(invite), vm.tabContent.value.invites)
    }

    private companion object {
        val guest = PublicUser(uuid = "b-uuid", username = "claude2", firstName = "Claude2")
        val invite = CollectionInvite(
            id = "i1", collectionId = "c1", collectionName = "Peru",
            ownerUsername = "claude", createdAt = "2026-09-16T08:45:08Z"
        )
        val trip = UltraSlimCollection(
            id = "c1", name = "Peru", description = "", isPublic = false, isArchived = false,
            createdAt = "", updatedAt = "", startDate = null, endDate = null, adventureCount = 0,
            featuredImage = null, link = null
        )
    }
}
